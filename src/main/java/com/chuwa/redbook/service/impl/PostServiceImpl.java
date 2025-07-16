package com.chuwa.redbook.service.impl;

import com.chuwa.redbook.dao.PostRepository;
import com.chuwa.redbook.entity.Post;
import com.chuwa.redbook.exception.DuplicateTitleException;
import com.chuwa.redbook.exception.ResourceNotFoundException;
import com.chuwa.redbook.payload.PostDto;
import com.chuwa.redbook.payload.PostResponse;
import com.chuwa.redbook.service.PostService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author b1go
 * @date 8/22/22 6:56 PM
 */
@Service
public class PostServiceImpl implements PostService {
    // Add logger from slf4j
    private static final Logger logger = LoggerFactory.getLogger(PostServiceImpl.class);

    @Autowired
    private PostRepository postRepository;

    /**
     * use this modelMapper to replace the mapToDto, mapToEntity methods.
     */
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public PostDto createPost(PostDto postDto) {
        logger.info("Creating new post with title: '{}'", postDto.getTitle());
        logger.debug("Post creation request details: {}", postDto);
        // Check for duplicate title using a regular exception handler
//        if (postRepository.existsByTitle(postDto.getTitle())) {
//            throw new IllegalArgumentException("Post title already exists: " + postDto.getTitle());
//        }

        // Check for duplicate title using a custom API exception handler
        if (postRepository.existsByTitle(postDto.getTitle())) {
            throw new DuplicateTitleException(postDto.getTitle());
        }

        // covert DTO to Entity
//        Post post = mapToEntity(postDto);
        Post post = modelMapper.map(postDto, Post.class);

        // 调用Dao的save 方法，将entity的数据存储到数据库MySQL
        // save()会返回存储在数据库中的数据
        Post savedPost = postRepository.save(post);
        logger.info("Successfully created post with ID: {} and title: '{}'",
                savedPost.getId(), savedPost.getTitle());
        // 将save() 返回的数据转换成controller/前端 需要的数据，然后return给controller
//        PostDto postResponse = mapToDTO(savedPost);

        return modelMapper.map(savedPost, PostDto.class);
    }

    /**
     * 此处练习了lambda， stream API
     * @return
     */
    @Override
    public List<PostDto> getAllPost() {
        List<Post> posts = postRepository.findAll();
        List<PostDto> postDtos = posts.stream().map(post -> modelMapper.map(post, PostDto.class)).collect(Collectors.toList());
        return postDtos;
    }

    /**
     * 此处顺便练习Optional
     * @param id
     * @return
     */
    @Override
    public PostDto getPostById(long id) {
        logger.info("Retrieving post with ID: {}", id);
//        Optional<Post> post = postRepository.findById(id);
//        post.orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));

//        Post post = postRepository.findById(id).get();

        Post post = postRepository.findById(id).orElseThrow(() -> {
            logger.warn("Cannot get the post: post not found with ID: {}", id);
            return new ResourceNotFoundException("Post", "id", id);
        });

        return modelMapper.map(post, PostDto.class);
    }

    @Override
    public PostDto updatePost(PostDto postDto, long id) {
        logger.info("Updating post with ID: {} and new title: '{}'", id, postDto.getTitle());
        logger.debug("Post update request details: {}", postDto);
        //  Question, why do we need to find it out firstly?
        Post post = postRepository.findById(id).orElseThrow(() -> {
            logger.warn("Cannot update the post: post not found with ID: {}", id);
            return new ResourceNotFoundException("Post", "id", id);
        });
        post.setTitle(postDto.getTitle());
        post.setDescription(postDto.getDescription());
        post.setContent(postDto.getContent());

        Post updatePost = postRepository.save(post);
        return modelMapper.map(updatePost, PostDto.class);
    }

    @Override
    public void deletePostById(long id) {
        logger.info("Deleting post with ID: {}", id);
        //  Question, why do we need to find it out firstly?
        Post post = postRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));
        postRepository.delete(post);
    }

    @Override
    public PostResponse getAllPost(int pageNo, int pageSize, String sortBy, String sortDir) {
        logger.info("Retrieving posts with pagination - Page: {}, Size: {}, Sort: {} {}", pageNo, pageSize, sortBy, sortDir);

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        // create pageable instance

        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, sort);
//        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));
//        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        Page<Post> pagePosts = postRepository.findAll(pageRequest);
        logger.info("Found {} posts out of {} total posts for page {}", pagePosts.getNumberOfElements(), pagePosts.getTotalElements(), pageNo);

                // get content for page abject
        List<Post> posts = pagePosts.getContent();
        List<PostDto> postDtos = posts.stream().map(post -> modelMapper.map(post, PostDto.class)).collect(Collectors.toList());

        PostResponse postResponse = new PostResponse();
        postResponse.setContent(postDtos);
        postResponse.setPageNo(pagePosts.getNumber());
        postResponse.setPageSize(pagePosts.getSize());
        postResponse.setTotalElements(pagePosts.getTotalElements());
        postResponse.setTotalPages(pagePosts.getTotalPages());
        postResponse.setLast(pagePosts.isLast());
        logger.debug("Created PostResponse with {} posts, page {}/{}", postDtos.size(), pagePosts.getNumber() + 1, pagePosts.getTotalPages());
        return postResponse;
    }
}
