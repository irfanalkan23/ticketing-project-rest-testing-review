package com.cydeo.service;


import com.cydeo.dto.UserDTO;
import com.cydeo.exception.TicketingProjectException;


import java.util.List;

public interface UserService{

    List<UserDTO> listAllUsers();
    UserDTO findByUserName(String username);
    UserDTO save(UserDTO dto);
    //we changed save() method from void to UserDTO, for showing the testing flow
    UserDTO update(UserDTO dto);
    void deleteByUserName(String username);
    void delete(String username) throws TicketingProjectException;
    List<UserDTO> listAllByRole(String role);





}
