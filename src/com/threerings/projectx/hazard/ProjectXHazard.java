package com.threerings.projectx.hazard;

import com.samskivert.util.ak;
import com.threerings.projectx.client.ProjectXApp;
import com.threerings.util.X;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

/**
 * Bootstraps instead of {@link ProjectXApp} for loading mods and language packs.
 * <p>
 * Activates when there is a file named "mods.conf" in the "/mods" directory.
 * <p>
 * Makes sure the "META-INF/MANIFEST.MF" is included in each mod jars,
 * and the main class must be specified.
 *
 * @author Leego Yih
 */
public class ProjectXHazard {
    private static final String USER_DIR = System.getProperty("user.dir");
    private static final String MODS_DIR = USER_DIR + "/mods/";
    private static final String MODS_CONF_PATH = MODS_DIR + "/mods.conf";
    private static final String MANIFEST_PATH = "META-INF/MANIFEST.MF";
    private static final String MAIN_CLASS_KEY = "Main-Class:";
    private static final String NAME_KEY = "Name:";
    public static ProjectXApp app;

    public static void main(String[] args) throws Exception {
        // Initializing
        System.setProperty("com.threerings.io.enumPolicy", "ORDINAL");
        if (ak.gm()) {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        }
        X.dM("projectx.log");
        // Load mods
        Config config = loadConfig();
        if (config != null) {
            loadMods(config);
        }
        // Process args
        String ticket = null;
        String password;
        for (int i = 0; i < args.length; ++i) {
            if ((password = args[i]).startsWith("+connect=")) {
                ticket = password;
                args = com.samskivert.util.c.b(args, i, 1);
                break;
            }
        }
        String username = args.length > 0 ? args[0] : System.getProperty("username");
        password = args.length > 1 ? args[1] : System.getProperty("password");
        boolean encrypted = Boolean.getBoolean("encrypted");
        String knight = args.length > 2 ? args[2] : System.getProperty("knight");
        String action = args.length > 3 ? args[3] : System.getProperty("action");
        String arg = args.length > 4 ? args[4] : System.getProperty("arg");
        String sessionKey = System.getProperty("sessionKey");
        // Instantiate the ProjectXApp and hold it
        Constructor<ProjectXApp> constructor = ProjectXApp.class.getDeclaredConstructor(String.class, String.class, boolean.class, String.class, String.class, String.class, String.class, String.class);
        constructor.setAccessible(true);
        app = constructor.newInstance(username, password, encrypted, knight, action, arg, sessionKey, ticket);
        app.startup();
    }

    static Config loadConfig() {
        File file = new File(MODS_CONF_PATH);
        if (!file.exists()) {
            System.out.println("Cannot find '" + MODS_CONF_PATH + "'");
            return null;
        }
        String content = readFile(file);
        if (content == null || content.length() == 0) {
            System.out.println("No mods loaded");
            return null;
        }
        List<String> jars = new ArrayList<String>();
        String[] lines = content.split("\n");
        for (String line : lines) {
            String s = line.trim();
            // Remove commented or non-jar lines
            if (!s.startsWith("--")
                    && !s.startsWith("#")
                    && s.endsWith(".jar")) {
                jars.add(s);
            }
        }
        if (jars.size() == 0) {
            return null;
        }
        return new Config(jars);
    }

    static void loadMods(Config config) {
        System.out.println("Mods path: " + MODS_DIR);
        List<File> jars = new ArrayList<File>();
        for (String jar : config.getJars()) {
            File file = new File(MODS_DIR + "/" + jar);
            if (file.exists()) {
                jars.add(file);
            } else {
                System.out.println("Cannot find jar '" + jar + "'");
            }
        }
        if (jars.isEmpty()) {
            return;
        }
        loadJars(jars);
        loadClasses(jars);
    }

    static void loadJars(List<File> jars) {
        Method method;
        try {
            method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        boolean accessible = method.isAccessible();
        method.setAccessible(true);
        for (File jar : jars) {
            try {
                method.invoke(classLoader, jar.toURI().toURL());
                System.out.println("Loaded jar '" + jar.getName() + "'");
            } catch (Exception e) {
                System.out.println("Failed to load jar '" + jar.getName() + "'");
                e.printStackTrace();
            }
        }
        method.setAccessible(accessible);
    }

    static void loadClasses(List<File> jars) {
        for (File jar : jars) {
            String manifest = readZip(jar, MANIFEST_PATH);
            if (manifest == null || manifest.length() == 0) {
                System.out.println("Failed to read '" + MANIFEST_PATH + "' from '" + jar.getName() + "'");
                continue;
            }
            String className = null;
            String modName = null;
            for (String item : manifest.split("\n")) {
                if (item.startsWith(MAIN_CLASS_KEY)) {
                    className = item.replace(MAIN_CLASS_KEY, "").trim();
                } else if (item.startsWith(NAME_KEY)) {
                    modName = item.replace(NAME_KEY, "").trim();
                }
            }
            if (className == null || className.length() == 0) {
                System.out.println("Failed to read 'Main-Class' from '" + jar.getName() + "'");
                continue;
            }
            System.out.println("Mod '" + modName + "' initializing");
            try {
                Class.forName(className);
                System.out.println("Mod '" + modName + "' initialized");
            } catch (Exception e) {
                System.out.println("Failed to load mod '" + modName + "'");
                e.printStackTrace();
            }
        }
    }

    static String readZip(File file, String entry) {
        StringBuilder sb = new StringBuilder();
        try {
            ZipFile zip = new ZipFile(file);
            InputStream is = zip.getInputStream(zip.getEntry(entry));
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String s;
            while ((s = reader.readLine()) != null) {
                sb.append(s).append("\n");
            }
            reader.close();
            zip.close();
            return sb.toString();
        } catch (Exception e) {
            System.out.println("Failed to read '" + file.getName() + "'");
            e.printStackTrace();
            return null;
        }
    }

    static String readFile(File file) {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String s;
            while ((s = reader.readLine()) != null) {
                sb.append(s).append("\n");
            }
            reader.close();
            return sb.toString();
        } catch (Exception e) {
            System.out.println("Failed to read '" + file.getName() + "'");
            e.printStackTrace();
            return null;
        }
    }

    static class Config {
        private final List<String> jars;

        public Config(List<String> jars) {
            this.jars = jars;
        }

        public List<String> getJars() {
            return jars;
        }
    }
}
