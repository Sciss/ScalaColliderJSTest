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

    sbt fastLinkJS
    
    python -m SimpleHTTPServer
    
    xdg-open http://127.0.0.1:8000

In the browser console, after booting:

    dumpOSC()
    
    bubbles()
    
    setControl("freq2", 444)
    
    serverCounts()

    dumpTree()

    cmdPeriod()
