package io.apef.base.utils;

import io.apef.testing.unit.BaseUnitSpec;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class BytesResourceTest extends BaseUnitSpec {
    @Test
    public void testFullPathResource() {
        Bytes resourceUrl = Bytes.wrap("http://localhost:9090/resource.index?param=a");
        BytesResource resource = new BytesResource(resourceUrl);
        assertEquals(resource.resourceUrl(), resourceUrl);
        assertEquals(resource.resourcePath(), Bytes.wrap("http://localhost:9090/"));
        assertEquals(resource.resourceName(), Bytes.wrap("resource.index?param=a"));
        assertEquals(resource.resourceParams(), Bytes.wrap("param=a"));
    }

    @Test
    public void testNoResource() {
        String resourceUrl = "http://localhost:9090/";
        BytesResource resource = new BytesResource(resourceUrl);
        assertEquals(resource.resourcePath(), Bytes.wrap("http://localhost:9090/"));
        assertNull(resource.resourceName());
        assertNull(resource.resourceParams());
    }

    @Test
    public void testOnlyNameResource() {
        String resourceUrl = "resource.index?param=a";
        BytesResource resource = new BytesResource(resourceUrl);
        assertNull(resource.resourcePath());
        assertEquals(resource.resourceName(), Bytes.wrap("resource.index?param=a"));
        assertEquals(resource.resourceParams(), Bytes.wrap("param=a"));
    }

    @Test
    public void testRootPathResource() {
        String resourceUrl = "/resource.index?param=a";
        BytesResource resource = new BytesResource(resourceUrl);
        assertEquals(resource.resourcePath(), Bytes.wrap("/"));
        assertEquals(resource.resourceName(), Bytes.wrap("resource.index?param=a"));
        assertEquals(resource.resourceParams(), Bytes.wrap("param=a"));
    }

    @Test
    public void testEmptyNameResource1() {
        String resourceUrl = "http://localhost:9090/?";
        BytesResource resource = new BytesResource(resourceUrl);
        assertEquals(resource.resourcePath(), Bytes.wrap("http://localhost:9090/"));
        assertNull(resource.resourceName());
        assertNull(resource.resourceParams());
    }

    @Test
    public void testEmptyNameResource2() {
        String resourceUrl = "http://localhost:9090/?param=a";
        BytesResource resource = new BytesResource(resourceUrl);
        assertEquals(resource.resourcePath(), Bytes.wrap("http://localhost:9090/"));
        assertEquals(resource.resourceName(), Bytes.wrap("?param=a"));
        assertEquals(resource.resourceParams(), Bytes.wrap("param=a"));
    }

    @Test
    public void testEmptyNameResource3() {
        String resourceUrl = "?param=a";
        BytesResource resource = new BytesResource(resourceUrl);
        assertNull(resource.resourcePath());
        assertEquals(resource.resourceName(), Bytes.wrap("?param=a"));
        assertEquals(resource.resourceParams(), Bytes.wrap("param=a"));
    }

    @Test
    public void testResourceNameNoParams() {
        Bytes resourceUrl = Bytes.wrap("http://localhost:9090/aaa/bbb/resource.index?param=a");
        BytesResource resource = new BytesResource(resourceUrl);
        assertEquals(resource.resourceUrl(), resourceUrl);
        assertEquals(resource.resourcePath(), Bytes.wrap("http://localhost:9090/aaa/bbb/"));
        assertEquals(resource.resourceName(), Bytes.wrap("resource.index?param=a"));
        assertEquals(resource.resourceParams(), Bytes.wrap("param=a"));
        assertEquals(resource.resourceNameNoParams(), Bytes.wrap("resource.index"));
    }

    @Test
    public void testResourceNameNoParamsReal() {
        Bytes resourceUrl = Bytes.wrap("http://localhost:9090/aaa/bbb/resource.index");
        BytesResource resource = new BytesResource(resourceUrl);
        assertEquals(resource.resourceUrl(), resourceUrl);
        assertEquals(resource.resourcePath(), Bytes.wrap("http://localhost:9090/aaa/bbb/"));
        assertEquals(resource.resourceName(), Bytes.wrap("resource.index"));
        assertNull(resource.resourceParams());
        assertEquals(resource.resourceNameNoParams(), Bytes.wrap("resource.index"));
    }

    @Test
    public void testResourceNameNoParamsReal2() {
        Bytes resourceUrl = Bytes.wrap("aaa/bbb/resource.index");
        BytesResource resource = new BytesResource(resourceUrl);
        assertEquals(resource.resourceUrl(), resourceUrl);
        assertEquals(resource.resourcePath(), Bytes.wrap("aaa/bbb/"));
        assertEquals(resource.resourceName(), Bytes.wrap("resource.index"));
        assertNull(resource.resourceParams());
        assertEquals(resource.resourceNameNoParams(), Bytes.wrap("resource.index"));
    }

    @Test
    public void testResourceUrlNoParams() {
        Bytes resourceUrl = Bytes.wrap("http://localhost:9090/aaa/bbb/resource.index?param=a");
        BytesResource resource = new BytesResource(resourceUrl);
        assertEquals(resource.resourceUrl(), resourceUrl);
        assertEquals(resource.resourcePath(), Bytes.wrap("http://localhost:9090/aaa/bbb/"));
        assertEquals(resource.resourceName(), Bytes.wrap("resource.index?param=a"));
        assertEquals(resource.resourceParams(), Bytes.wrap("param=a"));
        assertEquals(resource.resourceNameNoParams(), Bytes.wrap("resource.index"));
        assertEquals(resource.resourceUrlNoParams(), Bytes.wrap("http://localhost:9090/aaa/bbb/resource.index"));

        resourceUrl = Bytes.wrap("http://localhost:9090/aaa/bbb/resource.index");
        resource = new BytesResource(resourceUrl);
        assertEquals(resource.resourceUrl(), resourceUrl);
        assertEquals(resource.resourcePath(), Bytes.wrap("http://localhost:9090/aaa/bbb/"));
        assertEquals(resource.resourceName(), Bytes.wrap("resource.index"));
        assertEquals(resource.resourceParams(),null);
        assertEquals(resource.resourceNameNoParams(), Bytes.wrap("resource.index"));
        assertEquals(resource.resourceUrlNoParams(), Bytes.wrap("http://localhost:9090/aaa/bbb/resource.index"));
    }
}