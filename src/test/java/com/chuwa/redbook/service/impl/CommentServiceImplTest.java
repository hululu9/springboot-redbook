package com.chuwa.redbook.service.impl;

import com.chuwa.redbook.dao.CommentRepository;
import com.chuwa.redbook.dao.PostRepository;
import com.chuwa.redbook.entity.Comment;
import com.chuwa.redbook.entity.Post;
import com.chuwa.redbook.exception.BlogAPIException;
import com.chuwa.redbook.exception.ResourceNotFoundException;
import com.chuwa.redbook.payload.CommentDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@ExtendWith(MockitoExtension.class)
public class CommentServiceImplTest {
    private static final Logger logger = LoggerFactory.getLogger(CommentServiceImplTest.class);
    // 用 @Mock 创建 CommentRepository, PostRepository, ModelMapper 的假对象
    @Mock
    private CommentRepository commentRepositoryMock;

    @Mock
    private PostRepository postRepositoryMock;

    @Mock
    private ModelMapper modelMapperMock;
    // 用 @InjectMocks 创建 CommentServiceImpl 实例，自动注入这些 mock
    @InjectMocks
    private CommentServiceImpl commentService;

    private Post post;
    private CommentDto commentDto;
    private Comment comment;

    @BeforeAll
    static void beforeAll() {
        logger.info("START test");
    }
    // 共享数据对象，供测试使用
    @BeforeEach
    void setUp() {
        logger.info("set up Post and Comment for each test");
        this.post = new Post();
        post.setId(1L);

        this.comment = new Comment(1L, "Alice", "alice123@gmail.com", "The post is really helpful, love it!");
        comment.setPost(post);
        comment.setCreateDateTime(LocalDateTime.now());
        comment.setUpdateDateTime(LocalDateTime.now());

        this.commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setName("Alice");
        commentDto.setEmail("alice123@gmail.com");
        commentDto.setBody("The post is really helpful, love it!");
    }

    @Test
    public void testCreateComment() {
        // 为每个 test 方法配置不同场景的模拟行为（when(...).thenReturn(...)）
        // ArgumentMatchers.any() / eq() is optional
        Mockito.when(modelMapperMock.map(ArgumentMatchers.any(CommentDto.class), ArgumentMatchers.eq(Comment.class))).thenReturn(comment);
        Mockito.when(modelMapperMock.map(comment, CommentDto.class)).thenReturn(commentDto);
        Mockito.when(postRepositoryMock.findById(1L)).thenReturn(Optional.ofNullable(post));
        Mockito.when(commentRepositoryMock.save(comment)).thenReturn(comment);

        // 调用真正的 service 方法，并通过 Assertions 验证返回值或者异常
        CommentDto commentResponse = commentService.createComment(1L, commentDto);

        Assertions.assertNotNull(commentResponse);
        Assertions.assertEquals(commentDto.getName(), commentResponse.getName());
        Assertions.assertEquals(commentDto.getEmail(), commentResponse.getEmail());
        Assertions.assertEquals(commentDto.getBody(), commentResponse.getBody());
    }

    @Test
    public void testCreateComment_ResourceNotFoundException() {
        Mockito.when(postRepositoryMock.findById(ArgumentMatchers.anyLong()))
                .thenThrow(new ResourceNotFoundException("Post", "id", 1L));

        Assertions.assertThrows(ResourceNotFoundException.class, () -> commentService.createComment(1L, commentDto));
    }

    @Test
    public void testGetCommentsByPostId() {
        // comments size = 1, just one comment
        List<Comment> comments = new ArrayList<>();
        comments.add(comment);

        Mockito.when(commentRepositoryMock.findByPostId(1L)).thenReturn(comments); // List.of(comment)
        Mockito.when(modelMapperMock.map(comment, CommentDto.class)).thenReturn(commentDto);

        List<CommentDto> commentDtos = commentService.getCommentsByPostId(1L);

        // verifies commentDtos is not null before further operations（commentDtos.size()） are performed
        Assertions.assertNotNull(commentDtos);
        Assertions.assertEquals(1, commentDtos.size());
        CommentDto commentResponse = commentDtos.get(0);
        Assertions.assertEquals(commentDto.getName(), commentResponse.getName());
        Assertions.assertEquals(commentDto.getEmail(), commentResponse.getEmail());
        Assertions.assertEquals(commentDto.getBody(), commentResponse.getBody());
    }

    @Test
    public void testGetCommentById() {
        Mockito.when(postRepositoryMock.findById(1L)).thenReturn(Optional.of(post));
        Mockito.when(commentRepositoryMock.findById(1L)).thenReturn(Optional.of(comment));
        Mockito.when(modelMapperMock.map(comment, CommentDto.class)).thenReturn(commentDto);
        CommentDto commentResponse = commentService.getCommentById(1L, 1L);

        Assertions.assertNotNull(commentResponse);
        Assertions.assertEquals(commentDto.getName(), commentResponse.getName());
        Assertions.assertEquals(commentDto.getEmail(), commentResponse.getEmail());
        Assertions.assertEquals(commentDto.getBody(), commentResponse.getBody());
    }

    @Test
    public void testGetCommentsByPostId_PostNotFoundException() {
        Mockito.when(postRepositoryMock.findById(1L)).thenThrow(new ResourceNotFoundException("Post", "id", 1L));
        Assertions.assertThrows(ResourceNotFoundException.class, () -> commentService.getCommentById(1L, 1L));
    }

    @Test
    public void testGetCommentsByPostId_CommentNotFoundException() {
        Mockito.when(postRepositoryMock.findById(1L)).thenReturn(Optional.of(post));
        Mockito.when(commentRepositoryMock.findById(1L)).thenThrow(new ResourceNotFoundException("Comment", "id", 1L));
        Assertions.assertThrows(ResourceNotFoundException.class, () -> commentService.getCommentById(1L, 1L));
    }

    @Test
    public void testGetCommentsByPostId_BlogAPIException() {
        Post post2 = new Post();
        post2.setId(2L);
        comment.setPost(post2);

        Mockito.when(postRepositoryMock.findById(1L)).thenReturn(Optional.of(post));
        Mockito.when(commentRepositoryMock.findById(1L)).thenReturn(Optional.of(comment));

        Assertions.assertThrows(BlogAPIException.class, () -> commentService.getCommentById(1L, 1L));
    }

    @Test
    public void testUpdateComment() {
        Mockito.when(postRepositoryMock.findById(1L)).thenReturn(Optional.of(post));
        Mockito.when(commentRepositoryMock.findById(1L)).thenReturn(Optional.of(comment));

        CommentDto updatedCommentDto = new CommentDto();
        updatedCommentDto.setId(comment.getId());
        updatedCommentDto.setName("Alice Updated");
        updatedCommentDto.setEmail("updated@gmail.com");
        updatedCommentDto.setBody("Updated content!");

        Mockito.when(commentRepositoryMock.save(comment)).thenReturn(comment);
        Mockito.when(modelMapperMock.map(comment, CommentDto.class)).thenReturn(updatedCommentDto);
        CommentDto commentResponse = commentService.updateComment(1L, 1L, updatedCommentDto);

        Assertions.assertNotNull(commentResponse);
        Assertions.assertEquals(updatedCommentDto.getName(), commentResponse.getName());
        Assertions.assertEquals(updatedCommentDto.getEmail(), commentResponse.getEmail());
        Assertions.assertEquals(updatedCommentDto.getBody(), commentResponse.getBody());
    }

    @Test
    public void testUpdateComment_PostNotFoundException() {
        Mockito.when(postRepositoryMock.findById(1L)).thenThrow(new ResourceNotFoundException("Post", "id", 1L));
        Assertions.assertThrows(ResourceNotFoundException.class, () -> commentService.updateComment(1L, 1L, commentDto));
    }

    @Test
    public void testUpdateComment_CommentNotFoundException() {
        Mockito.when(postRepositoryMock.findById(1L)).thenReturn(Optional.of(post));
        Mockito.when(commentRepositoryMock.findById(1L)).thenThrow(new ResourceNotFoundException("Comment", "id", 1L));
        Assertions.assertThrows(ResourceNotFoundException.class, () -> commentService.updateComment(1L, 1L, commentDto));
    }

    @Test
    public void testUpdateComment_BlogAPIException() {
        Post post2 = new Post();
        post2.setId(2L);
        comment.setPost(post2);

        Mockito.when(postRepositoryMock.findById(1L)).thenReturn(Optional.of(post));
        Mockito.when(commentRepositoryMock.findById(1L)).thenReturn(Optional.of(comment));

        Assertions.assertThrows(BlogAPIException.class, () -> commentService.updateComment(1L, 1L, commentDto));
    }

    @Test
    public void testDeleteComment() {
        Mockito.when(postRepositoryMock.findById(1L)).thenReturn(Optional.of(post));
        Mockito.when(commentRepositoryMock.findById(1L)).thenReturn(Optional.of(comment));
        Mockito.doNothing().when(commentRepositoryMock).delete(comment);

        commentService.deleteComment(1L, 1L);
        // 用 Mockito.verify(...) 验证 repository 是否被调用，以及调用次数
        Mockito.verify(commentRepositoryMock, Mockito.times(1)).delete(comment);
    }

    @Test
    public void testDeleteComment_PostNotFoundException() {
        Mockito.when(postRepositoryMock.findById(1L)).thenThrow(new ResourceNotFoundException("Post", "id", 1L));
        Assertions.assertThrows(ResourceNotFoundException.class, () -> commentService.deleteComment(1L, 1L));
    }

    @Test
    public void testDeleteComment_CommentNotFoundException() {
        Mockito.when(postRepositoryMock.findById(1L)).thenReturn(Optional.of(post));
        Mockito.when(commentRepositoryMock.findById(1L)).thenThrow(new ResourceNotFoundException("Comment", "id", 1L));
        Assertions.assertThrows(ResourceNotFoundException.class, () -> commentService.deleteComment(1L, 1L));
    }

    @Test
    public void testDeleteComment_BlogAPIException() {
        Post post2 = new Post();
        post2.setId(2L);
        comment.setPost(post2);

        Mockito.when(postRepositoryMock.findById(1L)).thenReturn(Optional.of(post));
        Mockito.when(commentRepositoryMock.findById(1L)).thenReturn(Optional.of(comment));

        Assertions.assertThrows(BlogAPIException.class, () -> commentService.deleteComment(1L, 1L));
    }

}
