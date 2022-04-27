# ForceScreenshot

## Package name
`projekt.cloud.piece.force.screenshot`

## Features
Disable `screenshot is not allowed` features defined by application with following actions\
  1. Remove `WindowsManager.FLAG_SECURE` flag from argument(s) in `Window.setFlags()` and `Window.addFlags()` of `Window`
  2. Block `WindowsManager.FLAG_SECURE` flag argument(s) in `Window.setFlags()` and `Window.addFlags()` of `Window`
  3. Clear `WindowsManager.FLAG_SECURE` flag when `Activity.setContentView()` called by application
  4. Block `SurfaceView.setSecure()` method when argumtent set is true

## Requirement
This is a [Xposed](https://github.com/rovo89/Xposed) module, 
which requires [Xposed](https://github.com/rovo89/Xposed) environment to operate.

[LSPosed](https://github.com/LSPosed/LSPosed) environment is tested, and prefered.