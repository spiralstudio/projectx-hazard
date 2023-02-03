# projectx-hazard for Spiral Knights

It supports mods and adds more languages.

## Usage

Put [projectx-hazard.jar](dist/projectx-hazard.jar), [getdown.txt](dist/getdown.txt) and [digest.txt](dist/digest.txt) into Spiral Knights directory: 

```
Spiral Knights
├─code
│ └─projectx-hazard.jar (New)
├─mods
│ ├─mods.conf (New)
│ ├─mod1.jar  (New)
│ └─mod2.jar  (New)
├─getdown.txt (Modified)
└─digest.txt  (Modified)
```

## Mods

Create a file named [mods.conf](dist/mods/mods.conf) in the `Spiral Knights/mods/` directory.
And configure jar filenames, separate multiple filenames via newline character, for example:

```text
mod1.jar
mod2.jar
```

And you can use `#` or `--` comment lines:

```text
#mod3.jar
--mod4.jar
```

Make sure the `META-INF/MANIFEST.MF` is included in the mod jar, and the main class must be specified.

```manifest
Manifest-Version: 1.0
Main-Class: com.spiralstudio.mod.test.TestMod
```

More mods: https://github.com/spiralstudio/mods

## License

This project is under the MIT license. See the [LICENSE](LICENSE) file for details.

