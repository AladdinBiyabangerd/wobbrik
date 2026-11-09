# City raster / floor review — partial, imagegen only

Branch: `docs/city-raster-floor-qa`, based on `docs/missing-city-designs-v1`.
Reviewed scope: three versioned raster sources, QA script/report/composites, README,
prompt record and handoff. Original six boards remain unchanged; separate02-parts-flow-v2.png added. No runtime code.

Built-in imagegen produced v2 sources. All six v2 towers visually counted as ground
entrance plus nine upper inhabited courses, roof excluded; numbered overlay inspected.
Each source composited against token navy and light text background and viewed.
Broad backdrop halos are reduced; alpha 1–7 noise remains. Near-opaque generated
interiors often use alpha 252–254; true fully opaque coverage is not assumed.

Important finding: nominal equal-cell atlas coordinates are unsafe. Long road crosses
the nominal column boundary; explicit inspection regions and alpha>=8 bounds are now
recorded for each of 30 objects. These are QA bounds, not shipping crop metadata.
No ground-anchor calibration or arbitrary-height facade module set exists yet.
Plan-road proportions/L notch still differ from exact collision dimensions; geometry
must remain authoritative. `land_1` board still draws an elongated ghost rather than
the approved 220×220 square. No geometry correction is claimed.

Four board edit attempts and a second detail edit failed exact floor counts. Detail
still has eleven, parts preview nine, collection hero extra level, inventory counts
inconsistent. Discarded attempts were not promoted or copied over existing boards.
User answered the async question: imagegen ONLY. Python/Pillow is used solely for
QA measurement/composites, never source-art edits. Reference-based board correction
continues with the verified ten-floor atlas.

Checks: `qa/check_rasters.py` executed successfully; report records dimensions,
SHA-256, alpha distribution, 30 source bounds and no-upscale density 1/2/3/4 ceilings.
Three dark/light composites and numbered floor overlay viewed. Token contrast:
16.57 / 8.22 / 11.45, all PASS >=4.5. `git diff --check` PASS.
No Android build/emulator, adaptive font/phone/tablet checks or API tests: art-only.
No AppBackend change required (no domain/API/schema/economy change).
No branch CI, main merge, push, deployment or stage acceptance.

Next: complete board floor consistency using imagegen and the
verified tower sources while retaining layouts, then inspect final boards and finish
source contour/anchor/density QA. This unit is explicitly **not complete**.

## Imagegen-only continuation outcome

User choice honored: source images changed ONLY with built-in imagegen. Six more
reference-based attempts covered01/02/04/06, square land and road proportions;
then two single-detail passes restored02 windows/roof and targeted04's lowest
upper storey.02 passes count (nine upper + entrance); rectangular windows, smooth
cream plaster and violet roof restored. Saved as02-parts-flow-v2.png. Board is
fully opaque (alpha255); floor centers/hashes in QA JSON, numbered overlay viewed.
Its preview follows the source atlas more regularly than the old tower; no actual
attempt geometry or design approval is claimed.

04 still has11 after the last exact-region removal request.01 hero has extra
courses;06 compressed source towers and inserted extra city buildings. These
failed outputs not promoted. Land ghost remains visibly elongated rather than
exact square; road dimensions unchanged in selected source. Both attempts
rejected. Prompt sets v3/v4 retained; discarded image outputs left outside repo.
README links02 to the new candidate; all other board links unchanged.

Final focused check: QA script rerun with cached Pillow using uv --offline PASS;
all three source composites and both floor-count overlays viewed. Source art was
never edited by the QA script. diff check PASS. No CI/merge/push. Next bounded unit:
imagegen-only04 exact floor removal, without another permission question. Full
request remains incomplete; anchors/high-density modules remain explicit limits.
