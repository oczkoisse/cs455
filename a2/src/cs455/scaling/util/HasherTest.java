package cs455.scaling.util;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;

public class HasherTest {

	@Before
	public void setUp() throws Exception {
		
	}

	@Test
	public void testHash() {
		fail("Not yet implemented");
	}

	@Test
	public void testHashAsString() {
		String s = "The quick brown fox jumps over the lazy cog";
		String h = Hasher.hashAsString(ByteBuffer.wrap(s.getBytes(StandardCharsets.US_ASCII)));
		
		assertEquals(h, "de9f2c7fd25e1b3afad3e85a0bd17d9b100db4b3");
	}

	@Test
	public void testConvHashToString() {
		fail("Not yet implemented");
	}

}
