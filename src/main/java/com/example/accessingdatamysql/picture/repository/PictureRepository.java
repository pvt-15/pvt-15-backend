package com.example.accessingdatamysql.picture.repository;

import com.example.accessingdatamysql.model.User;
import com.example.accessingdatamysql.picture.entity.Picture;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PictureRepository extends CrudRepository<Picture, Integer> {
    List<Picture> findByUser(User user);
}
