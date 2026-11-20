# Folio design direction

Folio is a feature-preserving redesign of FileExplorer. Its visual and interaction reference is the user's Monomail project, interpreted through current stable Jetpack Compose and Material 3 Expressive APIs.

## Product principles

- Preserve every existing file-management capability and its module boundary unless a separately reviewed change proves a capability is redundant.
- Design the information architecture around file tasks, not around a generic dashboard.
- Prefer first-party AndroidX and Material 3 Expressive components over bespoke primitives.
- Keep file operations, permissions, provider capability checks, transfer state, and error reporting behavior unchanged during visual refactors.
- All builds and validation run in GitHub Actions. Do not require local or on-device compilation.

## Visual language

- Use Material You dynamic colour with complete light and dark colour schemes and accessible contrast.
- Use expressive but disciplined rounded geometry, tonal surface layering, and restrained elevation.
- Use consistent spacing and typography tokens from the design system. Avoid one-off dimensions in feature screens.
- Keep content surfaces calm and legible. File names, paths, metadata, selection state, and transfer progress take priority over decoration.
- Use shape and colour to communicate hierarchy and state, not to decorate every container.

## Navigation and layout

- Adapt Monomail's detached floating navigation treatment to file-manager information architecture.
- Keep primary navigation compact and visually separated from the main content surface.
- Keep the primary contextual action separate from the navigation surface.
- Preserve fast access to locations, tabs, bookmarks, search, selection actions, and advanced tools.
- Use adaptive layouts for compact phones, expanded phones, tablets, and desktop-sized windows without hiding core actions.

## Motion

- Use Material 3 Expressive motion APIs and Compose animation primitives with purposeful timing and easing.
- Prioritize continuity for navigation, folder changes, selection, search, sheets, and contextual actions.
- Use shared-element or container-transform patterns only where the source and destination have a clear semantic relationship.
- Prefer native shape morphing where it improves continuity; avoid decorative morphs that delay file operations.
- Respect reduced-motion settings and keep scrolling and large file lists stable.
- Never animate a file operation as if it succeeded before the underlying operation reports success.

## Component policy

- Prefer stable Material 3 Expressive components available in the selected Compose BOM.
- Build small reusable wrappers only to enforce Folio tokens, accessibility, or consistent behavior.
- Avoid duplicating stock components merely to change colours, radii, or padding.
- Any custom navigation or morphing component must have UI tests for selected state, back navigation, accessibility semantics, and state restoration.

## Migration sequence

1. Establish a reproducible GitHub Actions baseline and record the upstream feature inventory.
2. Upgrade the Android/Compose toolchain in a dedicated, build-verified change.
3. Audit the existing design-system module and map Monomail patterns onto Folio's navigation and file workflows.
4. Implement app shell, navigation, theme, and motion foundation before restyling individual feature screens.
5. Migrate feature screens incrementally, with regression coverage for storage providers, permissions, archives, network shares, cloud integrations, vault, editor, app manager, and transfers.
6. Validate debug and release variants in GitHub Actions; release signing secrets must never be required for debug builds.

## Non-goals

- Removing advanced functionality to simplify the interface.
- Replacing the Kotlin/Compose UI with C++.
- Introducing a new UI toolkit or an unnecessary design-system dependency.
- Requiring local Android Studio, Termux, or device-side builds.
