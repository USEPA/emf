
@echo off & setlocal

set inf=rundll32 setupapi,InstallHinfSection DefaultInstall

start/w %inf% 132 C:/Users/training/tmp/shortcut.inf

endlocal
