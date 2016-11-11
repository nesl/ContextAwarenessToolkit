package edu.ucla.nesl.toolkit.util;

/**
 * Created by cgshen on 11/10/16.
 * Based on jpmml-model
 * https://github.com/jpmml/jpmml-mode
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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
        // Get the pmml file name
        if (args.length != 1) {
            System.out.println("Error: please pass in the path to a pmml model.");
            System.exit(1);
        }
        File pmmlFile = new File(args[0]);
        File serFile = new File(args[0] + ".ser");

        // Parse a pmml object from a file
        PMML pmml;
        try (InputStream is = new FileInputStream(pmmlFile)) {
            Source source = ImportFilter.apply(new InputSource(is));
            pmml = JAXBUtil.unmarshalPMML(source);
        }

        // Apply visitors
        StringInterner stringInterner = new StringInterner();
        stringInterner.applyTo(pmml);
        LocatorNullifier locatorNullifier = new LocatorNullifier();
        locatorNullifier.applyTo(pmml);

        // Write an ser file from the pmml object
        try (OutputStream os = new FileOutputStream(serFile)) {
            SerializationUtil.serializePMML(pmml, os);
        }
    }
}
