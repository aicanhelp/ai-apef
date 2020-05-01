package io.apef.base.utils;

import io.apef.testing.unit.BaseUnitSpec;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class ResourceTest extends BaseUnitSpec {
    @Test
    public void testFullPathResource() {
        String resourceUrl = "http://localhost:9090/resource.index?param=a";
        Resource resource = new Resource(resourceUrl);
        assertEquals(resource.resourceUrl(), resourceUrl);
        assertEquals(resource.resourcePath(), "http://localhost:9090/");
        assertEquals(resource.resourceName(), "resource.index?param=a");
        assertEquals(resource.resourceParams(), "param=a");
    }

    @Test
    public void testNoResource() {
        String resourceUrl = "http://localhost:9090/";
        Resource resource = new Resource(resourceUrl);
        assertEquals(resource.resourcePath(), "http://localhost:9090/");
        assertNull(resource.resourceName());
        assertNull(resource.resourceParams());
    }

    @Test
    public void testOnlyNameResource() {
        String resourceUrl = "resource.index?param=a";
        Resource resource = new Resource(resourceUrl);
        assertNull(resource.resourcePath());
        assertEquals(resource.resourceName(), "resource.index?param=a");
        assertEquals(resource.resourceParams(), "param=a");
    }

    @Test
    public void testRootPathResource() {
        String resourceUrl = "/resource.index?param=a";
        Resource resource = new Resource(resourceUrl);
        assertEquals(resource.resourcePath(), "/");
        assertEquals(resource.resourceName(), "resource.index?param=a");
        assertEquals(resource.resourceParams(), "param=a");
    }

    @Test
    public void testEmptyNameResource1() {
        String resourceUrl = "http://localhost:9090/?";
        Resource resource = new Resource(resourceUrl);
        assertEquals(resource.resourcePath(), "http://localhost:9090/");
        assertNull(resource.resourceName());
        assertNull(resource.resourceParams());
    }

    @Test
    public void testEmptyNameResource2() {
        String resourceUrl = "http://localhost:9090/?param=a";
        Resource resource = new Resource(resourceUrl);
        assertEquals(resource.resourcePath(), "http://localhost:9090/");
        assertEquals(resource.resourceName(), "?param=a");
        assertEquals(resource.resourceParams(), "param=a");
    }

    @Test
    public void testEmptyNameResource3() {
        String resourceUrl = "?param=a";
        Resource resource = new Resource(resourceUrl);
        assertNull(resource.resourcePath());
        assertEquals(resource.resourceName(), "?param=a");
        assertEquals(resource.resourceParams(), "param=a");
    }
}