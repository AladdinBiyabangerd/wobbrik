# Working notes

## Branch, commit və lokal CI

- Hər məntiqi vahid uyğun `feat/`, `fix/`, `chore/` və ya `docs/` branch-ində hazırlanır; `main` üzərində implementasiya edilmir. Commit-lər kiçik, məzmununa uyğun və review edilə bilən olur.
- Hər branch bitəndə diff üzrə lokal review edilir. Tapılan problemlər düzəldilir və lokal CI yenidən işlədilir; bütün tələb olunan yoxlamalar uğurlu olana qədər dövr davam edir. Uğursuz və ya icra edilməmiş yoxlama keçmiş sayılmır.
- CI yalnız lokaldır: `./scripts/local-ci.sh`. GitHub Actions/remote CI yaradılmır. Arrowz-dakı vahid Gradle `ci` task yanaşması nümunədir, onun oyun qaydaları köçürülmür.
- Review qeydi `docs/reviews/` altında branch, yoxlanmış diff/commit, tapıntılar, düzəlişlər, CI nəticəsi və qalan məhdudiyyətləri göstərir. Son dəyişiklikdən sonra uyğun yoxlamalar yenilənir.
- Yalnız lokal review və CI uğurlu olduqdan sonra branch `main`-ə merge edilir. Merge nəticəsində CI təkrar yoxlanır; branch tarixi saxlanılır. Remote push/deploy ayrıca göstəriş tələb edir.
- AppBackend dəyişərsə onun ayrıca repository/branch/commit və öz AGENTS.md yoxlamaları da tələb olunur; Wobbrik CI nəticəsi backend yoxlamalarını əvəz etmir.

## AppBackend ilə uyğunluq

- Wobbrik-in serveri `/Users/mac/My Workspace/AppBackend` layihəsidir. 4 noyabr 2026 göstərişi ilə yekun Android oyununun Kotlin + Compose implementasiyasına icazə verilib; real SKU, Console və deploy ayrıca göstərişədək sonraya saxlanır.
- Wallet, oyun/nailiyyət mükafatları, progression/checkpoint, bina mülkiyyəti, bazar və şəhər axını qurulanda və ya yenilənəndə AppBackend-də uyğun domain qaydaları, API/DTO, schema/migration və test dəyişikliklərini də eyni işin hissəsi kimi yoxla və lazım olanları et. Yalnız mobil axını dəyişib server uyğunluğunu tamamlanmış sayma.
- Xərclənən bütün sikkələr və mülkiyyət üçün vahid authority AppBackend/PostgreSQL-dir. Client delta, Room və ya Firestore ikinci wallet yaratmır. Alış krediti, oyun mükafatı, xərcləmə və bazar transferi eyni wallet/ledger ilə işləməlidir.
- Müqavilə dəyişəndə backend və mobil uyğunluğunu, qayda versiyasını, revision/dedup, hesab izolyasiyası, retry və köhnə client davranışını yoxla; dəyişmiş API müqaviləsini sənədləşdir. Backend dəyişməsinə ehtiyac yoxdursa bunu yoxlama nəticəsində açıq qeyd et.
- Əsas müqavilə: `docs/kotlin-compose-plan.md`, xüsusən 5-ci bölmə. Backend alış sərhədi: `/Users/mac/My Workspace/AppBackend/docs/wobbrik-billing.md`. Tam domain adapterinin hazır olduğunu yalnız kod və yoxlamalar bunu təsdiqləyəndə bildir.

## Visual work

- Bütün ekran və oyun interfeysləri responsiv/adaptiv olmalıdır: həm telefonda, həm də planşetdə rahat və düzgün işləməlidir.
- Layout cihaz adına deyil, mövcud pəncərə ölçüsünə uyğunlaşmalıdır; portret, landşaft və ölçüsü dəyişən pəncərələr nəzərə alınmalıdır. Məzmun kəsilməməli, idarəetmələr üst-üstə düşməməli və əlçatmaz qalmamalıdır.
- Telefon dizaynını sadəcə böyütmək planşet üçün kifayət etmirsə, planşetə uyğun ayrıca kompozisiya/dizayn hazırla (məsələn, iki panel və ya uyğun naviqasiya); mövcud dizayn tokenlərini saxla.
- Vizual işi tamamlanmış saymazdan əvvəl telefon və planşet ölçülərində render edib görüntülərə bax; yoxlanmış ölçüləri və yoxlanmamış halları qeyd et.

When a task touches how something looks, use the `ui-craft` skill — it carries
the token, contrast and screenshot workflow. Two things hold regardless of it:

- **Find the token file before writing a value.** Inventing a colour, spacing or
  radius next to a palette that already names one is the most common way an
  interface drifts, and it never looks broken enough to get caught in review.
- **Don't report a visual change as working without having rendered it and
  looked at the image.** If rendering wasn't possible, say that plainly instead
  of implying the result was checked.

# Context growth

Optimize for minimum necessary context. One chat = one logical task.

## Warn and hand off when ANY is true

1. Chat completed 2+ distinct subtasks/features
2. Substantial implementation across 3+ files more than once
3. Long multi-step plan continuing in the same chat
4. History full of large diffs/tool output and next step is another big change
5. Topic switch (auth → payments → export → refactor) in the same chat

## Required actions (do not skip)

1. Finish only the current small logical unit if almost done
2. Update `docs/context/current-task.md` with: Completed / Current state / Decisions / Remaining work / Relevant files / Checks performed / Exact next step
3. Tell the user explicitly:
   `Context böyüyür — bu tapşırığı yeni chatdə davam et.`
   Then give a short continuation prompt (goal + key files + plan path if any)
4. Do not stack another large unrelated step in this chat after the warning

## Do not

- Rescan the whole repo when scoped files are enough
- Dump full conversations into context files — keep them short and reusable
- Start implementing a large planned feature in a chat that already did research + planning

## Continuity

- At the start of a continuation chat, read `docs/context/current-task.md` if present, then inspect only the referenced files needed for the task.
- Keep the handoff factual: include checks performed, unresolved issues, and the exact next step; do not invent completed work or context-usage percentages.
- Recommend a new chat without creating one automatically. If the user chooses to continue here, respect that choice and keep the handoff current.

