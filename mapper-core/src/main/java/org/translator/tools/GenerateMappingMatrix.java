package org.translator.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class GenerateMappingMatrix {

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("Usage: GenerateMappingMatrix <sourceClass> <targetClass> <outputCsv> [maxDepth]");
            System.exit(2);
        }
        String src = args[0];
        String tgt = args[1];
        String out = args[2];
        int maxDepth = args.length >= 4 ? Integer.parseInt(args[3]) : 6;

        Class<?> srcCls = Class.forName(src);
        Class<?> tgtCls = Class.forName(tgt);

        // read implemented mapping pairs preferably from the mapper source
        Map<String,String> srcToTgtMap = new HashMap<>();
        Map<String,Boolean> ignoredTargets = new HashMap<>();
        File mapperSrc = Paths.get("src/main/java/org/translator/mapper/Pacs008ToPacs009Mapper.java").toFile();
        if (mapperSrc.exists()) {
            Map<String,String> m = readMapperMappings(mapperSrc, ignoredTargets);
            if (m != null) srcToTgtMap.putAll(m);
        } else {
            srcToTgtMap.putAll(readExistingMappings(new File("docs/mapping_pairs_from_mapper.csv")));
        }

        LinkedHashMap<String,String> srcPaths = new LinkedHashMap<>(); // path -> type
        LinkedHashMap<String,String> tgtPaths = new LinkedHashMap<>();

        walkProperties(srcCls, decap(srcCls.getSimpleName()), 0, maxDepth, srcPaths, new HashSet<>());
        walkProperties(tgtCls, decap(tgtCls.getSimpleName()), 0, maxDepth, tgtPaths, new HashSet<>());

        Path outPath = Paths.get(out);
        if (outPath.getParent() != null) Files.createDirectories(outPath.getParent());

        try (PrintWriter pw = new PrintWriter(new FileWriter(outPath.toFile()))) {
            pw.println("sourcePath,sourceType,targetPath,targetType,mappingStrategy,status,testCaseId,notes");

            // for each source path, try to find mapping
            // We'll try exact suffix matching using mapper-extracted pairs first
            Set<String> consumedTargets = new HashSet<>();
            Set<String> consumedSources = new HashSet<>();

            // exact matches from mapper pairs (sourceFragment -> targetFragment)
            for (Map.Entry<String,String> pair : srcToTgtMap.entrySet()) {
                String srcFrag = pair.getKey();
                String tgtFrag = pair.getValue();
                // normalize fragments to full dotted paths when possible
                String srcTail = normalizeFragmentToPath(srcFrag, srcPaths);
                String tgtTail = normalizeFragmentToPath(tgtFrag, tgtPaths);
                String foundSrc = null;
                String foundTgt = null;
                String foundSrcType = "";
                String foundTgtType = "";
                for (String sp : srcPaths.keySet()) {
                    if (srcTail != null && (sp.equalsIgnoreCase(srcTail) || sp.endsWith("."+srcTail) || sp.endsWith("."+srcTail+"[]") || sp.endsWith(srcTail))) {
                        foundSrc = sp;
                        foundSrcType = srcPaths.get(sp);
                        break;
                    }
                }
                for (String tp : tgtPaths.keySet()) {
                    if (tgtTail != null && (tp.equalsIgnoreCase(tgtTail) || tp.endsWith("."+tgtTail) || tp.endsWith("."+tgtTail+"[]") || tp.endsWith(tgtTail))) {
                        foundTgt = tp;
                        foundTgtType = tgtPaths.get(tp);
                        break;
                    }
                }

                if (foundSrc != null) consumedSources.add(foundSrc);
                if (foundTgt != null) consumedTargets.add(foundTgt);

                String status;
                String mappingStrategy;
                String notes = "from-mapper-source" + (ignoredTargets.getOrDefault(tgtTail, false) ? ";ignored" : "");
                if (foundSrc != null && foundTgt != null) {
                    status = "done";
                    mappingStrategy = "direct/mapper-impl";
                    pw.printf("%s,%s,%s,%s,%s,%s,,%s\n", escape(foundSrc), escape(foundSrcType), escape(foundTgt), escape(foundTgtType), mappingStrategy, status, escape(notes));
                } else if (foundSrc != null) {
                    status = ignoredTargets.getOrDefault(tgtTail, false) ? "ignore" : "not-started";
                    mappingStrategy = "";
                    pw.printf("%s,%s,%s,%s,%s,%s,,%s\n", escape(foundSrc), escape(foundSrcType), "", "", mappingStrategy, status, escape(notes));
                } else if (foundTgt != null) {
                    status = ignoredTargets.getOrDefault(tgtTail, false) ? "ignore" : "not-started";
                    mappingStrategy = "";
                    pw.printf("%s,%s,%s,%s,%s,%s,,%s\n", "", "", escape(foundTgt), escape(foundTgtType), mappingStrategy, status, escape(notes));
                } else {
                    // no exact match found; emit a note to review
                    pw.printf("%s,%s,%s,%s,%s,%s,,%s\n", escape(srcTail), "", escape(tgtTail), "", "", "not-started", escape("mapper pair but no matching path found"));
                }
            }

            // emit remaining source paths not consumed
            for (Map.Entry<String,String> e : srcPaths.entrySet()) {
                if (consumedSources.contains(e.getKey())) continue;
                String sPath = e.getKey();
                String sType = e.getValue();
                // fallback heuristic: match by simple name
                String simple = lastSegment(sPath);
                String matchedTgtPath = "";
                String tType = "";
                for (String tp : tgtPaths.keySet()) {
                    if (tp.equalsIgnoreCase(simple) || tp.endsWith("."+simple) || tp.endsWith("."+simple+"[]") || tp.endsWith(simple)) {
                        matchedTgtPath = tp;
                        tType = tgtPaths.get(tp);
                        break;
                    }
                }
                String status = (matchedTgtPath.isEmpty() ? "not-started" : "done");
                String mappingStrategy = matchedTgtPath.isEmpty() ? "" : "heuristic-suffix";
                pw.printf("%s,%s,%s,%s,%s,%s,,%s\n", escape(sPath), escape(sType), escape(matchedTgtPath), escape(tType), mappingStrategy, status, escape(matchedTgtPath.isEmpty() ? "" : "heuristic match"));
            }

            // add target-only paths not already matched
            for (Map.Entry<String,String> e : tgtPaths.entrySet()) {
                String tp = e.getKey();
                boolean found = false;
                // check if any source row claimed this target
                for (String s : srcPaths.keySet()) {
                    String simple = lastSegment(s);
                    String mapped = srcToTgtMap.get(simple);
                    if (mapped != null) {
                        if (tp.equalsIgnoreCase(mapped) || tp.endsWith("."+mapped) || tp.endsWith("."+mapped+"[]")) { found = true; break; }
                    }
                    if (tp.equalsIgnoreCase(simple) || tp.endsWith("."+simple) || tp.endsWith("."+simple+"[]")) { found = true; break; }
                }
                if (!found) {
                    pw.printf(",,,%s,%s,not-started,,%s\n", escape(tp), escape(e.getValue()), "target-only");
                }
            }
        }

        System.out.println("Wrote mapping matrix to " + outPath.toAbsolutePath());
    }

    private static Map<String,String> readExistingMappings(File f) {
        Map<String,String> map = new HashMap<>();
        if (!f.exists()) return map;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            // skip header
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String tgt = parts[0].trim();
                    String src = parts[1].trim();
                    if (!src.isEmpty() && !tgt.isEmpty()) map.put(src, tgt);
                }
            }
        } catch (Exception e) { /* ignore */ }
        return map;
    }

    private static Map<String,String> readMapperMappings(File mapperSrc, Map<String,Boolean> ignoredTargets) {
        Map<String,String> map = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(mapperSrc))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("@Mapping(") && line.contains("target")) {
                    // collect possibly multi-line annotation
                    String ann = line;
                    if (!line.endsWith(")")) {
                        String more;
                        while ((more = br.readLine()) != null) {
                            ann += " " + more.trim();
                            if (more.trim().endsWith(")")) break;
                        }
                    }
                    String tgt = null;
                    String src = null;
                    boolean ign = false;
                    java.util.regex.Matcher mt = java.util.regex.Pattern.compile("target\\s*=\\s*\\\"([^\\\"]+)\\\"").matcher(ann);
                    if (mt.find()) tgt = mt.group(1).trim();
                    java.util.regex.Matcher ms = java.util.regex.Pattern.compile("source\\s*=\\s*\\\"([^\\\"]+)\\\"").matcher(ann);
                    if (ms.find()) src = ms.group(1).trim();
                    String annCompact = ann.replaceAll("\\s+"," ").toLowerCase();
                    if (annCompact.contains("ignore") && annCompact.contains("true")) ign = true;
                    if (tgt != null) {
                        if (ign) ignoredTargets.put(tgt, true);
                        if (src != null && !src.isEmpty()) {
                            // store mapping from full source tail to full target tail
                            map.put(src, tgt);
                        } else if (ign) {
                            // if ignore and no source, still note ignored target
                            map.put("", tgt);
                        }
                    }
                }
            }
        } catch (Exception e) {
            return map;
        }
        return map;
    }

    private static String lastSegment(String path) {
        if (path == null || path.isEmpty()) return path;
        int i = path.lastIndexOf('.');
        String seg = i >= 0 ? path.substring(i+1) : path;
        // strip array marker [] if present
        if (seg.endsWith("[]")) seg = seg.substring(0, seg.length()-2);
        return seg;
    }

    private static String normalizeFragmentToPath(String frag, LinkedHashMap<String,String> paths) {
        if (frag == null || frag.isEmpty()) return null;
        // try exact match against path keys
        for (String p : paths.keySet()) {
            if (p.equalsIgnoreCase(frag)) return p;
        }
        // try suffix longest-first
        String best = null;
        int bestLen = -1;
        for (String p : paths.keySet()) {
            if (p.equalsIgnoreCase(frag) || p.endsWith("."+frag) || p.endsWith("."+frag+"[]") || p.endsWith(frag)) {
                if (p.length() > bestLen) { best = p; bestLen = p.length(); }
            }
        }
        return best != null ? lastTail(best) : frag;
    }

    private static String lastTail(String full) {
        if (full == null) return null;
        int i = full.lastIndexOf('.');
        return i >= 0 ? full.substring(i+1) : full;
    }

    private static void walkProperties(Class<?> cls, String prefix, int depth, int maxDepth, LinkedHashMap<String,String> out, Set<Class<?>> visited) {
        if (cls == null) return;
        if (visited.contains(cls)) return;
        if (depth > maxDepth) return;
        visited.add(cls);

        Method[] methods = cls.getMethods();
        Arrays.sort(methods, (a,b)->a.getName().compareTo(b.getName()));
        for (Method m : methods) {
            String n = m.getName();
            if ((n.startsWith("get") || n.startsWith("is")) && m.getParameterCount() == 0 && !n.equals("getClass")) {
                String prop = toPropName(n);
                String full = prefix == null || prefix.isEmpty() ? prop : prefix + "." + prop;
                Class<?> ret = m.getReturnType();
                Class<?> elementType = null;
                Type gret = m.getGenericReturnType();
                if (java.util.Collection.class.isAssignableFrom(ret) && gret instanceof ParameterizedType) {
                    Type[] at = ((ParameterizedType) gret).getActualTypeArguments();
                    if (at != null && at.length > 0) {
                        if (at[0] instanceof Class) elementType = (Class<?>) at[0];
                    }
                }

                String typeName = elementType != null ? ("List<"+elementType.getSimpleName()+">") : ret.getSimpleName();
                out.put(full, typeName);

                Class<?> next = elementType != null ? elementType : ret;
                if (!isJdkType(next) && depth+1 <= maxDepth) {
                    walkProperties(next, full + (elementType != null ? "[]" : ""), depth+1, maxDepth, out, new HashSet<>(visited));
                }
            }
        }

        // also inspect public fields
        for (Field f : cls.getFields()) {
            String prop = f.getName();
            String full = prefix == null || prefix.isEmpty() ? prop : prefix + "." + prop;
            if (!out.containsKey(full)) out.put(full, f.getType().getSimpleName());
        }
    }

    private static boolean isJdkType(Class<?> c) {
        if (c == null) return true;
        if (c.isPrimitive()) return true;
        if (c.getPackage() == null) return false;
        String pkg = c.getPackage().getName();
        return pkg.startsWith("java.") || pkg.startsWith("javax.") || pkg.startsWith("jakarta.") || pkg.startsWith("org.w3c.") || pkg.startsWith("org.xml.") || pkg.startsWith("com.sun.");
    }

    private static String toPropName(String getter) {
        if (getter.startsWith("get")) return decap(getter.substring(3));
        if (getter.startsWith("is")) return decap(getter.substring(2));
        return getter;
    }

    private static String decap(String s) {
        if (s == null || s.isEmpty()) return s;
        if (s.length() > 1 && Character.isUpperCase(s.charAt(1)) && Character.isUpperCase(s.charAt(0))) return s;
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replaceAll(",", "\\,");
    }

}
