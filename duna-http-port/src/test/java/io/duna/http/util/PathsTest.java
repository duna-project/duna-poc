/*
 * Copyright (c) 2017 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.http.util;

import io.duna.core.service.Contract;
import io.duna.http.HttpInterface;
import io.duna.http.HttpPath;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.*;

/**
 * Created by carlos on 27/12/16.
 */
public class PathsTest {
    @Test
    public void getHttpInterfacePath() throws Exception {
        HttpInterface httpInterface = FooContract.class
            .getMethod("exposedMethod", int.class)
            .getAnnotation(HttpInterface.class);

        String path = Paths.getHttpInterfacePath(FooContract.class, httpInterface);
        assertEquals("/foo/exposedMethod/:param", path);
    }

    @Test
    public void getPathParameters() throws Exception {
        HttpInterface httpInterface = FooContract.class
            .getMethod("exposedMethod", int.class)
            .getAnnotation(HttpInterface.class);

        List<String> params = Paths.getPathParameters(Paths.getHttpInterfacePath(FooContract.class, httpInterface));
        assertEquals(1, params.size());
        assertThat(params, hasItem("param"));
    }

    @Test
    public void isExposed() throws Exception {
        assertTrue(Paths.isExposed(FooContract.class));
    }

    @Contract
    @HttpPath("/foo/")
    interface FooContract {

        @HttpInterface(path = "/exposedMethod/:param")
        void exposedMethod(int param);
    }
}
