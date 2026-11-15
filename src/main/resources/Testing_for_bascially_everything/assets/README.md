# GuessIt demo assets

Everything the demo needs at runtime lives in this package so the project is
self-contained.

```
Testing_for_bascially_everything/
  assets/
    posters/  13 poster thumbnails (540x810 PNG) for the animated wall
    clips/    bundled Lord of the Rings scenes used by the demo player
```

## Posters

`posters/<slug>.png` is loaded by
`Testing_for_bascially_everything.assets.AssetCatalog.poster(slug)` and used by
the Singleplayer poster wall. The slug is the lowercase title with runs of
non-alphanumeric characters replaced by `-`, e.g. `Dune: Part Two` becomes
`dune-part-two`. If a file is missing, the wall falls back to its gradient card,
so the app still starts.

The artwork is generated locally with no external image service:

```
java tools/GeneratePosters.java
```

That script rewrites every file in `posters/`. Edit the palette or motifs in
`tools/GeneratePosters.java` if you want different thumbnails.

## Clips

`clips/LordOfTheRings/scene1.mp4` through `scene3.mp4` are the bundled demo
clips. The play stage's **CHOOSE CLIP** menu lists them, and **From this
computer...** opens a file chooser that starts in this folder when the project
runs from source.

The VLC-backed `Movie_Playing_Page` reads its own folder from
`guessit.properties` (`clips.dir`). Point that property at

```
src/main/resources/Testing_for_bascially_everything/assets/clips
```

to reuse the bundled scenes there as well.
