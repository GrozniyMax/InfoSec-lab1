package com.grozniy.lab1.repository;

import com.grozniy.lab1.model.DataItem;
import com.grozniy.lab1.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class RepositoryTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DataItemRepository dataItemRepository;

    @Test
    void userRepositoryCrudAndLookups() {
        String username = "repo-test-user";
        User user = User.create(username, "some-hash");
        userRepository.save(user);

        Optional<User> found = userRepository.findByUsername(username);
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo(username);

        assertThat(userRepository.existsByUsername(username)).isTrue();
        assertThat(userRepository.existsByUsername("does-not-exist")).isFalse();
    }

    @Test
    void dataItemRepositoryFindAllOrderedByCreatedAtDesc() {
        DataItem older = DataItem.create("Older", "older content", null);
        dataItemRepository.save(older);
        DataItem newer = DataItem.create("Newer", "newer content", null);
        dataItemRepository.save(newer);

        List<DataItem> items = dataItemRepository.findAllByOrderByCreatedAtDesc();
        assertThat(items).isNotEmpty();
        assertThat(items)
                .extracting(DataItem::getCreatedAt)
                .isSortedAccordingTo(java.util.Comparator.reverseOrder());
        assertThat(items)
                .extracting(DataItem::getTitle)
                .contains("Older", "Newer");
    }
}