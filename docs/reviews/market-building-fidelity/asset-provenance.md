# Market material assets — 19 November 2026

Built-in `image_gen` tool, reference `docs/design/market-collection-v1/proposal.png`, opaque texture output. Each generated output was inspected and copied unchanged into the workspace; no complete building or UI image was generated or substituted. Runtime samples a small patch from a 4× sampled decode.

- Brick: `android-billing/app/src/main/assets/building-purple/market-brick-v1.png`, generated source `exec-ecf9c37b-8e98-43a5-bcd3-6a4c4482126f.png`; selected ID `base_2_wall`.
- Panel: `android-billing/app/src/main/assets/building-purple/market-panel-v1.png`, generated source `exec-a66b13b2-2659-4350-8909-c84c9d82e374.png`; selected ID `base_4_wall`.

## Final brick prompt

Use case: stylized-concept. Project asset, NOT a UI mockup. Reference image is the approved Wobbrik market screen. Generate only a square, perfectly flat, front-on seamless masonry material texture, edge to edge, no transparency needed. Match the FIRST red/plum brick tower's facade in the left market screen: rich warm raspberry/terracotta brick with muted magenta/plum shadows, fine pale warm peach mortar, softly beveled brick edges, warm painted game illustration finish. Regular horizontal running-bond brickwork, approximately 8 bricks across and 12 courses tall, subtle individual brick variation. Even illumination, no cast shadows across texture; lighting will be applied by the native renderer. NO building, NO windows, NO doors, NO trim or border, NO roof, NO plants, NO text, NO layout. This is a reusable modular wall material only. Fill the entire square with brickwork.

## Final panel prompt

Use case: stylized-concept. Project asset, NOT a UI mockup. Reference image: approved Wobbrik market screen. Generate only a square perfectly flat front-on seamless facade PANEL MATERIAL texture, edge to edge. Match the cool lavender/lilac metallic panel facade of the THIRD tower in the LEFT market screen, with warm pale peach thin panel seams and soft violet shadows. Large smooth rectangular panels in an orderly grid, about 4 panels across and 5 rows tall. Subtle small round fasteners near corners, fine bevels, softly painted polished game illustration finish, subtle material variation. Even illumination, no scene shadows; native renderer applies lighting. NO building, NO windows, NO doors, NO frame, NO trim, NO roof, NO plants, NO text, NO UI. Fill the entire square with lavender facade panels, not bricks or tiles. This is a reusable modular wall material only.

## File integrity

- Brick: 2050799 bytes, SHA-256 `881d8929b3d93014a65b3ac0280ef6de95936d995c27d814acd76ab85fa6cbe3`.
- Panel: 1836425 bytes, SHA-256 `5cf685dab70fe9f9b42f0bc7036f4b5bbaea02b881337eb18baba52790b033cc`.
