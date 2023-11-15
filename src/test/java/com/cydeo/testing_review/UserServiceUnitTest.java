package com.cydeo.testing_review;


import com.cydeo.dto.RoleDTO;
import com.cydeo.dto.UserDTO;
import com.cydeo.entity.Role;
import com.cydeo.entity.User;
import com.cydeo.mapper.UserMapper;
import com.cydeo.repository.UserRepository;
import com.cydeo.service.KeycloakService;
import com.cydeo.service.ProjectService;
import com.cydeo.service.TaskService;
import com.cydeo.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) //allows JUnit library to work with other libraries like Mockito.
public class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private TaskService taskService;

    @Mock
    private KeycloakService keycloakService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks    //inject the above dependencies to the main class I want to test
    private UserServiceImpl userService;

    @Spy    //Spy on the actual implementation of what is repeating and not changing (UserMapper)
    //I may check if UserMapper is called, the return type, etc.
    private UserMapper userMapper = new UserMapper(new ModelMapper());
    //in real implementation, ModelMapper is injected to UserMapper. we are creating new object here.

    //Create the test data we're going to use
    //since this test will be dealing with User and UserDTO; convert, findById, save() etc, we define the fields:
    User user;
    UserDTO userDTO;

    //JUnit5; @BeforeEach and @BeforeAll. (TestNG; @BeforeClass and @BeforeMethod)
    //executed before each test case.
    @BeforeEach
    void setUp(){
        user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUserName("user");
        user.setPassWord("Abc1");
        user.setEnabled(true);
        user.setRole(new Role("Manager"));

        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setFirstName("John");
        userDTO.setLastName("Doe");
        userDTO.setUserName("user");
        userDTO.setPassWord("Abc1");
        userDTO.setEnabled(true);

        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setDescription("Manager");

        userDTO.setRole(roleDTO);
    }

    private List<User> getUsers(){
        User user2 = new User();
        user2.setId(2L);
        user2.setFirstName("Emily");

        return List.of(user,user2);
    }

    private List<UserDTO> getUserDTOs(){
        UserDTO userDTO2 = new UserDTO();
        userDTO2.setId(2L);
        userDTO2.setFirstName("Emily");

        return List.of(userDTO,userDTO2);
    }

    //Test cases:
    @Test
    void should_list_all_users(){   //for testing the listAllUsers()

        //First, mock the "List<User> userList" service. (mock or stub whenever needed)
        //if the line is returning something, then I need to mock that, "if this method is called, return List of User"
        //stub (is used when a line (method) is returning something)
        when(userRepository.findAllByIsDeletedOrderByFirstNameDesc(false)).thenReturn(getUsers());

        List<UserDTO> expectedList = getUserDTOs();

        // "userService.listAllUsers()" is the method I'm testing. it returns list of dtos.
        List<UserDTO> actualList = userService.listAllUsers();

        //Assertions.assertEquals(expectedList,actualList);     //assertEquals() compares the object references, not the values of them

        //AssertJ : library is the solution to the problem I had above line
        //use assertThat (AssertJ library) for objects and collections! Compares the values for each field.
        assertThat(actualList).usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(expectedList);
        //import assertThat() from assertj.core library. not available in JUnit5.

    }

    //create test case for happy scenario.
    // method includes if() statement, so we'll create a negative scenario as well
    @Test
    void should_find_user_by_username(){
        //use stubbing because the line is returning something
        when(userRepository.findByUserNameAndIsDeleted(anyString(),anyBoolean())).thenReturn(user);

        //you may want to keep unnecessary stubbing, ie. for @ParameterizedTest. then use lenient()
        //lenient().when(userRepository.findAllByIsDeletedOrderByFirstNameDesc(false)).thenReturn(getUsers());

        //mockito.ArgumentMatchers issue:
        //if you use ArgumentMatchers for one argument (ex.anyString, you have to use ArgumentMatchers for all arguments
        //using "false" in other argument will create error
//        when(userRepository.findByUserNameAndIsDeleted(anyString(),false)).thenReturn(user);


        //call the actual method that we are testing (userService.findByUserName()).
        //it will use the user in above line (.thenReturn(user)) behind the scene
        //and we cannot use ArgumentMatchers (ex.anyString) here, because it is calling real method
        UserDTO actualUserDTO = userService.findByUserName("user");

        UserDTO expectedUserDTO = userDTO;

        assertThat(actualUserDTO).usingRecursiveComparison().isEqualTo(expectedUserDTO);
    }

    @Test
    void should_throw_exception_when_user_not_found(){
        //we can either stub null or do nothing since it will return null also.
        //when(userRepository.findByUserNameAndIsDeleted(anyString(),anyBoolean())).thenReturn(null);

        //we call the method and capture the exception and its message
        Throwable throwable = catchThrowable(()->userService.findByUserName("someUsername"));
        //we use assertInstanceOf method to verify exception type
        assertInstanceOf(NoSuchElementException.class,throwable);
        //we can verify exception message with assertEquals
        assertEquals("User not found",throwable.getMessage());
    }

    @Test
    void should_save_user(){
        //I'm not testing if the passwordEncoder is working properly or not,
        //I'm just saying, "when the passwordEncoder is called, return this password
        when(passwordEncoder.encode(anyString())).thenReturn("anypassword");

        //we don't need to stub mapper, we are using @Spy and using actual mapper
//        when(userMapper.convertToEntity(userDTO)).thenReturn(user);

        when(userRepository.save(any())).thenReturn(user);

        //real method to be tested. this should be after the stubbing because using stubs
        UserDTO actualDTO = userService.save(userDTO);

        //verify that keycloakService userCreate method is called (invoked).
        //no need to stub, because not returning anything (keycloakService.userCreate(dto);)
        verify(keycloakService).userCreate(any());

        assertThat(actualDTO).usingRecursiveComparison().isEqualTo(userDTO);
    }
}
