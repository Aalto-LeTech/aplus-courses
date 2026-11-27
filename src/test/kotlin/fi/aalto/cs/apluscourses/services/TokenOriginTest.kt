package fi.aalto.cs.apluscourses.services

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TokenOriginTest {
    @Test
    fun `credential keys normalize hosts and default ports`() {
        assertEquals("https://plus.cs.aalto.fi:443", tokenServiceKey("https://plus.cs.aalto.fi/api/v2"))
        assertEquals("https://plus.cs.aalto.fi:443", tokenServiceKey("https://plus.cs.aalto.fi:443"))
        assertEquals("https://plus.cs.aalto.fi:443", tokenServiceKey("https://PLUS.CS.AALTO.FI"))
        assertEquals(tokenServiceKey("https://Example.test"), tokenServiceKey("https://example.test"))
        assertNotEquals("https://plus.cs.aalto.fi:443", tokenServiceKey("http://plus.cs.aalto.fi"))
        assertNotEquals("https://plus.cs.aalto.fi:443", tokenServiceKey("https://plus.cs.aalto.fi:8443"))
        assertNotEquals("https://plus.cs.aalto.fi:443", tokenServiceKey("https://other.example"))
    }

    @Test
    fun `paths share credentials within an origin while ports stay separate`() {
        assertEquals(tokenServiceKey("http://localhost:8000/prefix"), tokenServiceKey("http://localhost:8000"))
        assertNotEquals(tokenServiceKey("http://localhost:8000"), tokenServiceKey("http://localhost:8001"))
    }
}
