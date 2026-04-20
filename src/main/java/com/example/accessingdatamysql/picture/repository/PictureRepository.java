package com.example.accessingdatamysql.picture.repository;

import com.example.accessingdatamysql.model.Picture;
import com.example.accessingdatamysql.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PictureRepository extends CrudRepository<Picture, Integer> {
    List<Picture> findByUser(User user);
}
