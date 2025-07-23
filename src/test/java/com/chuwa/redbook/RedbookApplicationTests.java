package com.chuwa.redbook;

import com.chuwa.redbook.payload.PostDto;
import com.chuwa.redbook.service.impl.PostServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class RedbookApplicationTests {

	@Autowired
	private PostServiceImpl postService;

	@Test
    /**
     * Encounter: LazyInitializationException, which occurs when:
     * 1. Hibernate loads an entity with lazy collections
     * 2. The Hibernate session closes
     * 3. Code tries to access the lazy collection outside the session
     * 4. Hibernate can't initialize the collection because no session exists
     * To fix it, add @Transactional to keep the session OPEN for the entire test method
     */
    @Transactional
	void testGetAllPosts() {
        // 1. postService.getAllPost(): This method call opens a NEW transaction/session
        // 2. After method returns, the transaction/session is CLOSED
        // 3. The Post entities have lazy-loaded 'comments' collections
        // 4. ModelMapper tries to convert Post -> PostDto
        // 5. ModelMapper accesses post.getComments()
        // 6. ERROR: No session available to load the lazy collection!
        // need to add @Transactional
		assertNotNull(postService.getAllPost());
	}

    @Test
    @Transactional
	void testCreatePost() {
        PostDto postDto = new PostDto();
        postDto.setTitle("title" + UUID.randomUUID());
        postDto.setContent("content");
        postDto.setDescription("description");
        postService.createPost(postDto);

        assertNotNull(postService.getAllPost());
    }


}
