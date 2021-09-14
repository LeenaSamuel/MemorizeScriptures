package com.top5.scriptures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.junit4.SpringRunner;

import com.top5.scriptures.model.Scripture;
import com.top5.scriptures.repository.ScripturesRepository;

@RunWith(SpringRunner.class)
@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class Top5ScripturesApplicationTests {
	
	@Autowired
    private ScripturesRepository sRepo;


	@Test
	void getScripturesByBibleVersionAndKeywordTest() {
		
	}

}
