/*
 * Copyright (c) 2016 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.gradle.tasks;

import io.duna.parsing.ParameterNameScanner;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.incremental.IncrementalTaskInputs;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ParseServiceSourceMetadataTask extends SourceTask {

    private File outputFile;

    Map<String, String> methodParameterNames = new ConcurrentHashMap<>();

    @TaskAction
    public void execute(IncrementalTaskInputs inputs) {
        DB db = DBMaker
            .fileDB(outputFile)
            .fileMmapEnable()
            .fileMmapPreclearDisable()
            .cleanerHackEnable()
            .make();

        ConcurrentMap<String, String> outputMap = db
            .hashMap("methodParameter", Serializer.STRING_ASCII, Serializer.STRING_ASCII)
            .createOrOpen();

        inputs.outOfDate(changed -> {
            try {
                CompilationUnit compilationUnit = JavaParser.parse(changed.getFile());
                outputMap.putAll(compilationUnit.accept(new ParameterNameScanner(), ""));
            } catch (Exception e) {
                getLogger().error("Error while parsing file " + source, e);
            }
        });

        db.close();
    }

    @OutputFile
    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    @SkipWhenEmpty
    @InputFiles
    @Override
    public FileTree getSource() {
        return super.getSource();
    }
}
