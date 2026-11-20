# Folio design and motion contract

Use [Monomail](https://github.com/skray9403/monomail) as the implementation reference:

- `ExpressivePersistentDock.kt`: one persistent floating pill dock, animated selected-state treatment, and a separate quick action.
- `PredictiveBack.kt`: foreground-card shrink and background reveal. Adapt this to Folio's navigation APIs rather than copying dependencies blindly.
- `Animations.kt`: centralized spring and tween motion tokens.
- `Theme.kt` and `shape.kt`: Material Expressive theme, wallpaper-derived colours, and a consistent rounded shape scale.

## Principles

1. Material 3 Expressive components are the default. Custom Compose UI is reserved for interactions native components do not provide.
2. Use dynamic colour on supported Android versions while retaining light, dark, and OLED options.
3. Keep one persistent navigation dock outside the destination NavHost.
4. Use a compact selected pill with a separate contextual FAB where a screen has a clear primary action.
5. Use restrained spring motion for navigation and selection. Avoid animating file rows in ways that cause visible list jumps.
6. Respect reduced-motion settings and keep animations short enough for high-density file workflows.
7. Retain storage, archive, network, root, Shizuku, cloud, security, transfer, editor, and app-management workflows.

## First-stage navigation

The floating dock exposes Browse, Network, Collections, and Transfers. The existing drawer remains the full navigation surface for advanced destinations. Search, sorting, selection actions, and the browser's New Folder action stay contextual to their existing screens.
