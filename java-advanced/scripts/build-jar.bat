set adv=../../java-advanced-2023
set my_impl=info/kgeorgiy/ja/kuznetsov/implementor/Implementor
set kga_impl=info/kgeorgiy/java/advanced/implementor
set impl=%adv%/modules/info.kgeorgiy.java.advanced.implementor/%kga_impl%
javac -d . -cp %adv%/lib/*;%adv%/artifacts/* ../java-solutions/%my_impl%.java %impl%/JarImpler.java %impl%/Impler.java %impl%/ImplerException.java
jar cmf MANIFEST.MF Implementor.jar %my_impl%.class %kga_impl%/JarImpler.class %kga_impl%/Impler.class %kga_impl%/ImplerException.class
rd /s /q info