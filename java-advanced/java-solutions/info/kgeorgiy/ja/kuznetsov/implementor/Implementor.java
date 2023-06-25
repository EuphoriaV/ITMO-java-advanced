package info.kgeorgiy.ja.kuznetsov.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.lang.System.lineSeparator;

/**
 * Represents Implementor
 *
 * @author Ilya Kuznetsov (ilyakuznecov84@gmail.com)
 * Class implements {@link JarImpler}
 */
public class Implementor implements JarImpler {

    /**
     * Default constructor
     */
    public Implementor() {
    }

    /**
     * Creates new java file with implementation of given interface.
     *
     * @param token given class.
     * @param root  path to place where to create new file.
     * @throws ImplerException if given class is not interface, or it is private, or in case of output error.
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (!token.isInterface() || Modifier.isPrivate(token.getModifiers())) {
            throw new ImplerException("Can't implement this interface: " + token.getCanonicalName());
        }
        Path fileName = root.resolve(token.getPackageName().
                replace(".", "/")).resolve(token.getSimpleName() + "Impl.java");
        try {
            if (fileName.getParent() != null) {
                Files.createDirectories(fileName.getParent());
            }
            BufferedWriter writer = Files.newBufferedWriter(fileName);
            writer.write(getClass(token));
            writer.close();
        } catch (IOException e) {
            throw new ImplerException("Invalid output file: " + fileName);
        }
    }

    /**
     * Returns java code {@link String} of implementation of given interface.
     * Created code contains line where package is declared, line where class is declared,
     * lines with all methods and close bracket.
     *
     * @param token given interface.
     * @return {@link String} code of implementation of given interface.
     */
    private String getClass(Class<?> token) {
        return getPackage(token) + getClassName(token) + getAllMethods(token) + "}";
    }

    /**
     * Returns java code {@link String} of package of implementation of given interface.
     * Method returns empty line if given interface doesn't have package or returns package otherwise.
     *
     * @param token given interface.
     * @return {@link String} line with package of implementation of given interface.
     */
    private String getPackage(Class<?> token) {
        if (token.getPackageName().equals("")) {
            return "";
        }
        return "package " + token.getPackageName() + ";" + lineSeparator() + lineSeparator();
    }

    /**
     * Returns java code {@link String} of all methods that implementation of given interface must implement.
     *
     * @param token given interface.
     * @return {@link String} all methods that class must implement.
     */
    private String getAllMethods(Class<?> token) {
        StringBuilder sb = new StringBuilder();
        var set = getAllSuperMethods(token);
        for (Method method : set) {
            sb.append(getMethod(method));
        }
        return sb.toString();
    }

    /**
     * Returns {@link Set<Method>} all methods that class must implement.
     *
     * @param token given interface.
     * @return {@link Set<Method>} all methods that class must implement.
     */
    private Set<Method> getAllSuperMethods(Class<?> token) {
        Set<Method> set = new TreeSet<>(Comparator.comparing(Method::getName).thenComparing(this::getParams));
        getAllSuperMethodsRecursive(token, set);
        return set;
    }

    /**
     * Recursive auxiliary method that is used in {@link #getAllSuperMethods(Class)} method
     * to get all methods that class must implement. Class have {@link Set<Method>}set as a parameter where we
     * store methods. We put there all declared methods and then call this method for super interfaces.
     *
     * @param token given interface.
     * @param set   set where we store methods.
     */
    private void getAllSuperMethodsRecursive(Class<?> token, Set<Method> set) {
        set.addAll(Arrays.asList(token.getDeclaredMethods()));
        for (Class<?> clazz : token.getInterfaces()) {
            getAllSuperMethodsRecursive(clazz, set);
        }
    }

    /**
     * Returns java code {@link String} of implementation of given method.
     *
     * @param method given method.
     * @return {@link String} java code of implementation of given method.
     */
    private String getMethod(Method method) {
        if (method.isDefault() || Modifier.isStatic(method.getModifiers())) {
            return "";
        }
        return "\t" + getMethodName(method) + lineSeparator() +
                "\t\t" + getReturn(method) + lineSeparator() +
                "\t}" + lineSeparator() + lineSeparator();
    }

    /**
     * Returns java code {@link String} of line where, what method returns is declared.
     *
     * @param method given method.
     * @return {@link String} java code of line where, what method returns is declared.
     */
    private String getReturn(Method method) {
        Class<?> returnType = method.getReturnType();
        if (returnType.isPrimitive()) {
            if (returnType.equals(void.class)) {
                return "";
            } else if (returnType.equals(boolean.class)) {
                return "return false;";
            } else {
                return "return 0;";
            }
        } else {
            return "return null;";
        }
    }

    /**
     * Returns java code {@link String} of given method declaration.
     *
     * @param method given method.
     * @return {@link String} java code of given method declaration.
     */
    private String getMethodName(Method method) {
        return "public " + method.getReturnType().getCanonicalName() + " " + method.getName() +
                getParams(method) + " {";
    }

    /**
     * Returns java code {@link String} of all parameters of given method.
     *
     * @param method given method.
     * @return {@link String} of all parameters of given method.
     */
    private String getParams(Method method) {
        return Arrays.stream(method.getParameters()).map(parameter -> parameter.getType().getCanonicalName() + " " +
                parameter.getName()).collect(Collectors.joining(", ", "(", ")"));
    }

    /**
     * Returns java code {@link String} of declaration of class that implements given interface.
     *
     * @param token given interface.
     * @return {@link String} of all parameters of given method.
     */
    private String getClassName(Class<?> token) {
        return "public class " + token.getSimpleName() + "Impl implements " +
                token.getCanonicalName() + " {" + lineSeparator();
    }

    /**
     * Creates new jar archive with java file with implementation of given interface.
     *
     * @param token   given class.
     * @param jarFile path to jar file that method is going to create.
     * @throws ImplerException in case of output error.
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        Path root = jarFile.getParent();
        implement(token, root);
        String classPath = token.getPackageName().replace(".", "/") + "/" + token.getSimpleName() + "Impl";
        compile(token, root.resolve(classPath + ".java"));
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(jarFile))) {
            ZipEntry zipEntry = new ZipEntry(classPath + ".class");
            zipOutputStream.putNextEntry(zipEntry);
            Files.copy(root.resolve(classPath + ".class"), zipOutputStream);
        } catch (IOException e) {
            throw new ImplerException("Invalid output file: " + jarFile);
        }
    }

    /**
     * Compiles given class which is located in given class path.
     *
     * @param clazz     given class.
     * @param classPath path to file with java code of given class.
     * @throws ImplerException in case of compilation error.
     */
    private void compile(Class<?> clazz, Path classPath) throws ImplerException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new ImplerException("Could not find java compiler, include tools.jar to classpath");
        }
        final String[] args;
        try {
            args = new String[]{
                    "-encoding", "UTF-8",
                    "-cp",
                    Path.of(clazz.getProtectionDomain().getCodeSource().getLocation().toURI()).toString(),
                    classPath.toString()
            };
        } catch (URISyntaxException e) {
            throw new ImplerException("Error occurred while compiling");
        }
        final int exitCode = compiler.run(null, null, null, args);
        if (exitCode != 0) {
            throw new ImplerException("Compiler exit code must be 0");
        }
    }

    /**
     * Main method of {@link Implementor} class.
     * If there are 2 arguments, method will implement given interface and locate it in given path.
     * If there are 3 arguments, method will create jar file in given path,
     * and jar file will contain implementation of given interface.
     *
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        try {
            if (args.length < 2 || args.length > 3 || args[0] == null ||
                    args[1] == null || (args.length == 3 && args[2] == null)) {
                throw new ImplerException("Invalid arguments");
            }
            Implementor implementor = new Implementor();
            if (args[0].equals("-jar")) {
                implementor.implementJar(Class.forName(args[1]), Path.of(args[2]));
            } else {
                implementor.implement(Class.forName(args[0]), Path.of(args[1]));
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Invalid class name: " + args[0]);
        } catch (ImplerException e) {
            System.err.println("Implementor exception: " + e.getMessage());
        }
    }
}
