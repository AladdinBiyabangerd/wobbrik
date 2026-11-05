# Yekun oyun məqsədi və main-only CI — 5 noyabr 2026

## Completed
- Engine və balance/rescue main-ə merge edilib.
- Təsdiqlənmiş dizaynların birəbir implementasiyası AGENTS.md-də məcburidir.
- Məqsəd yekun real Android oyunudur; ayrıca prototip/məşq tətbiqi deyil.
- CI qaydası: branch review → main-ə merge → yalnız main-də lokal CI. Uğursuzluq ayrıca fix branch ilə düzəldilir.

## Current state
- Compose host hələ əvvəlki işdən qalan uncommitted fayllardır; bu qayda düzəlişinə daxil deyil.
- MainActivity hazırda PracticeEngine, sxematik Canvas binaları, “Offline məşq” menyusu və yalnız session/saved-instance rekord istifadə edir. Process death yeni cəhd yaradır. Bu, yekun oyun kimi qəbul edilmir.
- Əvvəlki renderlər dizayna birəbir uyğunluğun sübutu deyil.

## Decisions
- Adları dəyişmək çatışmayan funksiyaları tamamlamır; engine davranışını sırf Practice adı var deyə silmək olmaz.
- Server wallet/progression authority-si qorunur; offline xal mükafat yaratmır. Real SKU/Console/deploy ayrıca göstəriş gözləyir.
- Qayda/CI vahidi AppBackend domain/API/schema dəyişmir; backend uyğunluğu və inteqrasiya tamamlanmış sayılmır.

## Remaining work
- Mövcud hostu yekun məhsul axınına çevirmək: təsdiqlənmiş qrafika, real menyu/naviqasiya, cəhdin bərpası və server gameplay adapteri.
- MainActivity/README-dəki məşq əhatəsini yalnız real əvəzləmə implementasiya ediləndən sonra yeniləmək.
- Telefon və planşetdə render/baxış, dizayn müqayisəsi; cihaz gameplay/performance yoxlamaları.

## Relevant files
- AGENTS.md; .cursor/rules/local-delivery.mdc; scripts/local-ci.sh
- docs/kotlin-compose-plan.md (xüsusən 5-ci bölmə və mərhələlər)
- android-billing/app/README.md; android-billing/app/src/main/kotlin/az/wobbrik/game/{MainActivity,GameTokens}.kt
- docs/design/game-tap-v2/README.md; docs/design/mobile-v1/tokens.css; docs/design/README.md
- /Users/mac/My Workspace/AppBackend/AGENTS.md; docs/wobbrik-game-api.md (backend repository daxilində)

## Checks performed
- Əvvəlki design-fidelity qaydası üçün main CI uğurlu idi.
- Bu vahid main-ə merge edildikdən sonra lokal CI keçdi (47 task, exit 0); review və CI nəticəsi docs/reviews/2026-11-05-product-scope-main-ci.md-də izlənir.
- Bu vahiddə UI dəyişdirilməyib və yeni render aparılmayıb.

## Exact next step
- Yeni chatdə əvvəlcə bu qeydi və istinad edilən host/dizayn/API sənədlərini oxu. Mövcud uncommitted hostu qoruyaraq yekun oyun üçün ilk kiçik implementasiya vahidini seç: server attempt lifecycle və bərpa müqaviləsini hosta bağlamaq; backend qaydalarını yoxlamadan lokal real mükafat əlavə etmə. Böyük implementasiyaya başlamazdan əvvəl ui-craft və backend AGENTS.md oxunmalıdır.
