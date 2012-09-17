WikipediaMobileJ2ME
===================
The official Wikipedia J2me Application

----------

BUILD NOTES

The project is built with NetBeans using LWUIT resources.  Version numbers: 
NetBeans: 7.1.1 
LWUIT: 1.5
Platform: Nokia SDK 1.1
CLDC: 1.1
MIDP: 2.1

Care must be taken when branching ./res/WikiResource.res.  It is a binary file which cannot be merged like a text file; thus, any conflicts cannot be resolved.  If you need to work in this file, be sure that you have the most recent version, and commit and push to master as soon as possible when you are done.

-----------

SUPPORT

If you need help with this application or would like to help out with
further development, please touch base in irc.freenode.net in the #wikimedia-mobile
room. Lots of people in there to help and discuss this application.

-----------

TRADEMARK NOTES

All Wikimedia and Wikipedia trademarks contained herein are NOT licensed
for use by any third-parties. Their inclusion in this open source software
is only for their eventual replacement if you distribute the application.

That is, the trademarks are protected, but the code itself is under an MIT 
license. You can use the trademarks individually, but not for any sort of
distribution.

---------

CONTRIBUTORS

Christopher Axthelm (original creator),
William Knight,
Eric Honour

---------

BUILDING AND TESTING

* on Windows 7 or 8
* Install 32-bit JDK 7 from http://www.oracle.com/technetwork/java/javase/downloads/index.html
** Nokia SDK needs a 32-bit JDK or JRE. 64-bit JDK doesn't currently install on Windows 8.
* Install NetBeans 7.2 with all options from: http://www.netbeans.org/
** If you already have NetBeans, you will need to install the Mobility plugin.
* Install Nokia 1.1 for Java SDK from http://www.developer.nokia.com/Develop/Java/Tools/
* Open the project in NetBeans.
* Ff necessary, right-click project and "Resolve Reference Errors"
** Add J2ME MIDP emulator -> point at Nokia SDK
* Run application.

The app will run in a Nokia emulator. It seems to include a touchscreen in the
profile, but note it's a bit tricky to use with the mouse.s

------------

Copyright (c) 2010-2011 Wikimedia Foundation

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.