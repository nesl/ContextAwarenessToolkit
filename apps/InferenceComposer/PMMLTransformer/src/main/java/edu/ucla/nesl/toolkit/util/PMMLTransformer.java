package edu.ucla.nesl.toolkit.util;

/**
 * Created by cgshen on 11/10/16.
 * Based on jpmml-model
 * https://github.com/jpmml/jpmml-mode
 */

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import javax.xml.transform.Source;

import org.dmg.pmml.PMML;
import org.jpmml.model.ImportFilter;
import org.jpmml.model.JAXBUtil;
import org.jpmml.model.SerializationUtil;
import org.jpmml.model.visitors.StringInterner;
import org.jpmml.model.visitors.LocatorNullifier;
import org.xml.sax.InputSource;

public class PMMLTransformer {
    public static void main(String[] args) throws Exception {
        // Get the pmml path
        if (args.length != 1) {
            System.out.println("Error: please pass in the path to pmml models.");
            System.exit(1);
        }

        // Traverse the directory and transform all PMML files
        Path pmmlPath = Paths.get(args[0]);
        FileVisitor<Path> pmmlVisitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                System.out.println(file.getFileName());
                if (file.getFileName().toString().endsWith(".pmml")) {
                    System.out.println(
                            "Tranforming " + file.getFileName() + " to SER...");

                    // Parse a pmml object from a file
                    PMML pmml;
                    try {
                        InputStream is = new FileInputStream(file.toFile());
                        Source source = ImportFilter.apply(new InputSource(is));
                        pmml = JAXBUtil.unmarshalPMML(source);

                        // Apply visitors
                        StringInterner stringInterner = new StringInterner();
                        stringInterner.applyTo(pmml);
                        LocatorNullifier locatorNullifier = new LocatorNullifier();
                        locatorNullifier.applyTo(pmml);

                        // Write an ser file from the pmml object
                        OutputStream os = new FileOutputStream(
                                new File(file.toFile().getAbsolutePath() + ".ser"));
                        SerializationUtil.serializePMML(pmml, os);
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        };
        Files.walkFileTree(pmmlPath, pmmlVisitor);
    }
}
