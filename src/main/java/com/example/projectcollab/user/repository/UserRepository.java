package com.example.projectcollab.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.projectcollab.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
