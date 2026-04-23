package com.example.accessingdatamysql.user.mapper;

import com.example.accessingdatamysql.model.User;
import com.example.accessingdatamysql.model.enums.Level;
import com.example.accessingdatamysql.model.enums.Provider;
import com.example.accessingdatamysql.user.dto.UserResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserMapperTest {

    private final UserMapper userMapper = new UserMapper();

    @Test
    void toUserResponse_shouldMapAllFields() {
        User user = new User();
        user.setId(1);
        user.setName("TestName");
        user.setEmail("Test.Name@example.com");
        user.setProvider(Provider.GOOGLE);
        user.setProviderUserId("google-123");
        user.setTotalPoints(120);
        user.setLevel(Level.LEVEL_2);

        UserResponse response = userMapper.toUserResponse(user);

        assertNotNull(response);
        assertEquals(1, response.getId());
        assertEquals("TestName", response.getName());
        assertEquals("Test.Name@example.com", response.getEmail());
        assertEquals(Provider.GOOGLE, response.getProvider());
        assertEquals("google-123", response.getProviderUserId());
        assertEquals(120, response.getTotalPoints());
        assertEquals(Level.LEVEL_2, response.getLevel());
    }

    @Test
    void toUserResponse_shouldReturnNull_whenUserIsNull() {
        UserResponse response = userMapper.toUserResponse(null);
        assertNull(response);
    }
}
