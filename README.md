# ScalaCollider JS Test

## statement

This is a project for testing [ScalaCollider](https://github.com/Sciss/ScalaCollider) compiled to JavaScript 
in the browser along with SuperCollider server (scsynth) compiled to WebAssembly.

SuperCollider is released under GNU GPL v2+, 
ScalaCollider is released under AGPL v3+.
Consequently, this project is released under AGPL v3+ as well.
It includes the binary wasm build of scsynth in the `lib` directory, for source code see
[here](https://github.com/Sciss/supercollider/tree/wasm).

## building and running

A demo may or may not be already running at [www.sciss.de/temp/scalacollider.js](https://www.sciss.de/temp/scalacollider.js/).

This git repository already contains the build `lib/main.js`, so you can actually skip
compilation unless you change the source code!

### compiling

The project uses [sbt](https://www.scala-sbt.org/) to build. Once installed, you can run

    sbt fastOptJS

(This overwrites and reproduces `lib/main.js`)

### running

Then start a local webserver from the project directory, e.g.
    
    python -m SimpleHTTPServer

(or use Node.js, Nginx, etc.)

And open the corresponding page in a browser, e.g.
    
    xdg-open http://127.0.0.1:8000

Hint: when recompiling, it's useful in Chrome/Chromium to disable the caching of js files:
In 'Developer tools', in tab 'Network', check 'Disable cache'. If you increment the `BUILD_NUMBER`
the source code, you can verify in the browser console that the latest build has indeed been loaded.

In the browser console (ctrl-shift-J in Chrome/Chromium), after booting the server ('Boot' button):

    dumpOSC()
    
    example('name')   // run example (invalid name prints available example names)
    
    sendOSC("/n_set", 1000, "freq1", 222.2) // send arbitrary OSC messages from JS
    
    serverCounts()

    dumpTree()

    cmdPeriod()

All the example code is in `ScalaColliderTest.scala`. To edit the project, you can use, for example,
[IntelliJ IDEA](https://www.jetbrains.com/idea/download/) with Scala plugin, or [VS Code/Codium](https://vscodium.com/)
with [Metals](https://scalameta.org/metals/) plugin.

For continuous development, you can run `sbt` without arguments, and on the sbt console enter `~fastOptJS`.
Then the compiler will always rebuild the project when it detects source code changes.
