package platform

import (
	"appbackend/internal/wobbrik"
	"context"
	"encoding/json"
	"fmt"
	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"
	"sort"
	"strings"
	"sync"
	"sync/atomic"
	"testing"
	"time"
)

type cityQueryTrace struct {
	count   atomic.Int64
	mu      sync.Mutex
	queries map[string]int
}

func (q *cityQueryTrace) TraceQueryStart(ctx context.Context, _ *pgx.Conn, data pgx.TraceQueryStartData) context.Context {
	q.count.Add(1)
	key := strings.Join(strings.Fields(data.SQL), " ")
	if len(key) > 110 {
		key = key[:110]
	}
	q.mu.Lock()
	q.queries[key]++
	q.mu.Unlock()
	return ctx
}
func (q *cityQueryTrace) TraceQueryEnd(context.Context, *pgx.Conn, pgx.TraceQueryEndData) {}
func TestIntegrationCityLatencyProbe(t *testing.T) {
	v, s := wobServer(t)
	ctx := context.Background()
	if err := s.EnsureAccount(ctx, "wobbrik", "alice"); err != nil {
		t.Fatal(err)
	}
	ids := []string{}
	for i := 0; i < 12; i++ {
		id := marketBuilding(t, s, "alice", fmt.Sprintf("cityprobe%02d", i))
		ids = append(ids, id)
		if _, err := s.DB.Exec(ctx, `INSERT INTO wobbrik_city_buildings(app,uid,building_id) VALUES('wobbrik','alice',$1)`, id); err != nil {
			t.Fatal(err)
		}
	}
	cfg := s.DB.Config()
	trace := &cityQueryTrace{queries: map[string]int{}}
	cfg.ConnConfig.Tracer = trace
	pool, err := pgxpool.NewWithConfig(ctx, cfg)
	if err != nil {
		t.Fatal(err)
	}
	old := s.DB
	s.DB = pool
	defer func() { s.DB = old; pool.Close() }()
	for sample := 0; sample < 3; sample++ {
		trace.count.Store(0)
		trace.queries = map[string]int{}
		start := time.Now()
		var bytes atomic.Int64
		requests := 0
		read := func(path string) []byte {
			w := wobRequest(v, "GET", "game/"+path, "alice", "")
			if w.Code != 200 {
				t.Errorf("%s status=%d %s", path, w.Code, w.Body.String())
			}
			bytes.Add(int64(w.Body.Len()))
			return w.Body.Bytes()
		}
		var first wobbrik.CityPage
		json.Unmarshal(read("city?limit=20"), &first)
		requests++
		if len(first.Buildings) != 12 {
			t.Fatal("membership", len(first.Buildings))
		}
		gate := make(chan struct{}, 4)
		var wg sync.WaitGroup
		for _, id := range ids {
			wg.Add(1)
			gate <- struct{}{}
			go func(id string) { defer wg.Done(); defer func() { <-gate }(); read("buildings/" + id) }(id)
			requests++
		}
		wg.Wait()
		read("decorations?limit=20")
		requests++
		read("city?limit=20")
		requests++
		t.Logf("CITY-HTTP sample=%d buildings=12 requests=%d sqlStatements=%d responseJSONBytes=%d elapsed=%s", sample, requests, trace.count.Load(), bytes.Load(), time.Since(start))
		if sample == 2 {
			keys := []string{}
			for k := range trace.queries {
				keys = append(keys, k)
			}
			sort.Strings(keys)
			for _, k := range keys {
				t.Logf("SQL count=%d prefix=%q", trace.queries[k], k)
			}
		}
	}
}
