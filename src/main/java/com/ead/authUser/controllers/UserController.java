package com.ead.authUser.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ead.authUser.dtos.UserDTO;
import com.ead.authUser.models.UserModel;
import com.ead.authUser.services.UserService;
import com.ead.authUser.specification.SpecificationTemplate;
import com.fasterxml.jackson.annotation.JsonView;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequestMapping("users")
public class UserController {

	@Autowired
	UserService userService;
	
	@GetMapping("allUsers")
	public ResponseEntity<Object> getAllUser(SpecificationTemplate.UserSpect spec,
										     @PageableDefault(page = 0, 
										     size = 2, 
										     sort = "userName",
										     direction = Sort.Direction.DESC) 
										     Pageable pageable){
		
		log.debug("GET getAllUser");

		Page<UserModel> pageUserModel = userService.findAllPageable(spec, pageable);
		
		if (pageUserModel.isEmpty()) {
			log.warn("List users not found");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("List users not found");
		}
		
		for (UserModel userModel : pageUserModel.toList()) {
			userModel.add(linkTo(methodOn(UserController.class).getOneUser(userModel.getUserId())).withRel("GET - User"));
			userModel.add(linkTo(methodOn(UserController.class).updateUser(userModel.getUserId(), null)).withRel("PUT - User"));
			userModel.add(linkTo(methodOn(UserController.class).deleteUser(userModel.getUserId())).withRel("DELETE - User"));
			userModel.add(linkTo(methodOn(AuthenticationController.class).registerUser(null)).withRel("POST - User"));
		}
		
		log.info("User list successfully retrieved");

		return ResponseEntity.status(HttpStatus.OK).body(pageUserModel);
	}
	
	@GetMapping("/{userId}")
	public ResponseEntity<Object> getOneUser(@PathVariable(value = "userId") UUID userId){
		
		log.debug("GET getOneUser userId {}", userId);

		var userModel = userService.findById(userId);
		
		if (userModel.isEmpty()) {
			log.warn("User not found");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
		}
		
		userModel.get().add(linkTo(methodOn(UserController.class).updateUser(userModel.get().getUserId(), null)).withRel("PUT - User"));
		userModel.get().add(linkTo(methodOn(UserController.class).deleteUser(userModel.get().getUserId())).withRel("DELETE - User"));
		userModel.get().add(linkTo(methodOn(AuthenticationController.class).registerUser(null)).withRel("POST - User"));
		userModel.get().add(linkTo(methodOn(UserController.class).getAllUser(null, null)).withRel("GET - AllUsers"));
		
		log.info("Usur found {}", userModel);

		return ResponseEntity.status(HttpStatus.OK).body(userModel);
	}
	
	@PutMapping("/{userId}")
	public ResponseEntity<Object> updateUser(@PathVariable(value = "userId") UUID userId,
											 @JsonView(UserDTO.UserView.UserPut.class)
									 		 @Validated(UserDTO.UserView.UserPut.class)
											 @RequestBody UserDTO userDTO){
		
		log.debug("PUT updateUser userId {}, userDTO {}", userId, userDTO);

		var userModel = userService.findById(userId);
		
		if (userModel.isEmpty()) {
			log.warn("User not found");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
		}
		
		userModel.get().setFullName(userDTO.getFullName());
		userModel.get().setPhoneNumber(userDTO.getPhoneNumber());
		userModel.get().setCpf(userDTO.getCpf());
		userModel.get().setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
		
		userService.save(userModel.get());
		
		userModel.get().add(linkTo(methodOn(UserController.class).getOneUser(userModel.get().getUserId())).withRel("GET - User"));
		userModel.get().add(linkTo(methodOn(UserController.class).deleteUser(userModel.get().getUserId())).withRel("DELETE - User"));
		userModel.get().add(linkTo(methodOn(AuthenticationController.class).registerUser(null)).withRel("POST - User"));
		userModel.get().add(linkTo(methodOn(UserController.class).getAllUser(null, null)).withRel("GET - AllUsers"));
		
		log.info("User successfully updated {}", userModel.get().toString());

		return ResponseEntity.status(HttpStatus.OK).body(userModel);
	}
	
	@DeleteMapping("/{userId}")
	public ResponseEntity<Object> deleteUser(@PathVariable(value = "userId") UUID userId){
		
		log.debug("DELETE deleteUser userId {}", userId);

		var userModel = userService.findById(userId);
		
		if (userModel.isEmpty()) {
			log.warn("User not found");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
		}
		
		userService.delet(userModel.get());
		
		userModel.get().add(linkTo(methodOn(UserController.class).getOneUser(userModel.get().getUserId())).withRel("GET - User"));
		userModel.get().add(linkTo(methodOn(UserController.class).updateUser(userModel.get().getUserId(), null)).withRel("PUT - User"));
		userModel.get().add(linkTo(methodOn(AuthenticationController.class).registerUser(null)).withRel("POST - User"));
		userModel.get().add(linkTo(methodOn(UserController.class).getAllUser(null, null)).withRel("GET - AllUsers"));
		
		log.info("User successfully deleted {}", userModel.get().toString());

		return ResponseEntity.status(HttpStatus.OK).body(userModel.get());
	}
}
