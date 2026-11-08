# Agent qaydaları və kontekst — lokal review

- Branch: `docs/wobbrik-agent-context`; baseline: `a887995` (main).
- Scope: baseline-dan bu review-u ehtiva edən commit-ə qədər AGENTS.md, `.cursor/rules/` və `.cursor/context/` diff-i. Lokal self-review; subagent yoxdur.
- Şablon Wobbrik-in texniki/məhsul planlarına bağlandı; placeholder stack və yeni paralel plan yaradılmadı.
- Mövcud branch/merge/main-only lokal CI, AppBackend authority və təsdiqlənmiş dizayn qaydaları saxlanıldı. Auto-push və təkrar design-sample gate köçürülmədi.
- Tapıntı/düzəliş: ikinci current-task status mənbəyi riski yönləndirici ilə aradan qaldırıldı; online branch-in dolayı main merge riski main əsaslı ayrıca worktree ilə aradan qaldırıldı. EOF whitespace düzəldildi.
- Yoxlamalar: altı Cursor rule body-si AGENTS.md-dəki uyğun bölmə ilə tam eynidir; alwaysApply frontmatter və əsas mənbə yolları yoxlandı. `git diff --check` PASS. Mövcud handoff dəyişdirilmədi.
- CI branch-də işə salınmayıb. `381362c` commit-i `686536a` ilə main-ə merge olunduqdan sonra `./scripts/local-ci.sh` PASS: BUILD SUCCESSFUL, 103 task (2 executed, 101 up-to-date); core/engine testləri UP-TO-DATE, təzə test icrası iddia edilmir. Log: `/private/tmp/wobbrik-agent-context-ci.log`. Bu nəticə qeydi merge edildikdən sonra eyni gate yenilənir: `/private/tmp/wobbrik-agent-context-final-ci.log`.
- Məhdudiyyət: tətbiq/backend kodu, API/schema və vizual implementasiya dəyişmir; backend dəyişikliyi lazım deyil. Render və canlı backend yoxlaması bu sənədləşmə işinə aid deyil; oyun mərhələsi qəbul edilmiş sayılmır. Remote push/deploy yoxdur.
