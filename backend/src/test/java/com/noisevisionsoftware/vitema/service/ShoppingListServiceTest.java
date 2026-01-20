package com.noisevisionsoftware.vitema.service;

import com.noisevisionsoftware.vitema.exception.NotFoundException;
import com.noisevisionsoftware.vitema.model.shopping.CategorizedShoppingListItem;
import com.noisevisionsoftware.vitema.model.shopping.ShoppingList;
import com.noisevisionsoftware.vitema.repository.ShoppingListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShoppingListServiceTest {

    @Mock
    private ShoppingListRepository shoppingListRepository;

    @InjectMocks
    private ShoppingListService shoppingListService;

    private ShoppingList sampleShoppingList;
    private static final String SAMPLE_ID = "test-id";
    private static final String SAMPLE_DIET_ID = "test-diet-id";
    private static final String SAMPLE_CATEGORY = "warzywa";

    @BeforeEach
    void setUp() {
        sampleShoppingList = ShoppingList.builder()
                .id(SAMPLE_ID)
                .dietId(SAMPLE_DIET_ID)
                .userId("test-user-id")
                .items(new HashMap<>())
                .createdAt(com.google.cloud.Timestamp.of(new Timestamp(Instant.now().toEpochMilli())))
                .startDate(com.google.cloud.Timestamp.of(new Timestamp(Instant.now().toEpochMilli())))
                .endDate(com.google.cloud.Timestamp.of(new Timestamp(Instant.now().toEpochMilli())))
                .version(1)
                .build();
    }

    @Test
    void getShoppingListByDietId_WhenExists_ShouldReturnShoppingList() {
        // given
        when(shoppingListRepository.findByDietId(SAMPLE_DIET_ID))
                .thenReturn(Optional.of(sampleShoppingList));

        // when
        ShoppingList result = shoppingListService.getShoppingListByDietId(SAMPLE_DIET_ID);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getDietId()).isEqualTo(SAMPLE_DIET_ID);
        verify(shoppingListRepository).findByDietId(SAMPLE_DIET_ID);
    }

    @Test
    void getShoppingListByDietId_WhenNotExists_ShouldReturnNull() {
        // given
        when(shoppingListRepository.findByDietId(SAMPLE_DIET_ID))
                .thenReturn(Optional.empty());

        // when
        ShoppingList result = shoppingListService.getShoppingListByDietId(SAMPLE_DIET_ID);

        // then
        assertThat(result).isNull();
        verify(shoppingListRepository).findByDietId(SAMPLE_DIET_ID);
    }

    @Test
    void saveShoppingList_ShouldCallRepository() {
        // when
        shoppingListService.saveShoppingList(sampleShoppingList);

        // then
        verify(shoppingListRepository).save(sampleShoppingList);
    }

    @Test
    void updateShoppingListItems_WhenExists_ShouldUpdateAndReturn() {
        // given
        Map<String, List<CategorizedShoppingListItem>> newItems = new HashMap<>();
        newItems.put(SAMPLE_CATEGORY, Collections.singletonList(
                CategorizedShoppingListItem.builder()
                        .name("Marchewka")
                        .quantity(1.0)
                        .unit("kg")
                        .build()
        ));

        when(shoppingListRepository.findById(SAMPLE_ID))
                .thenReturn(Optional.of(sampleShoppingList));

        when(shoppingListRepository.save(any(ShoppingList.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        ShoppingList result = shoppingListService.updateShoppingListItems(SAMPLE_ID, newItems);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getItems()).isEqualTo(newItems);
        verify(shoppingListRepository).save(sampleShoppingList);
    }

    @Test
    void updateShoppingListItems_WhenNotExists_ShouldThrowNotFoundException() {
        // given
        when(shoppingListRepository.findById(SAMPLE_ID))
                .thenReturn(Optional.empty());

        // when/then
        assertThatThrownBy(() ->
                shoppingListService.updateShoppingListItems(SAMPLE_ID, new HashMap<>())
        )
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(SAMPLE_ID);
    }

    @Test
    void removeItemFromCategory_WhenExistsAndValidIndex_ShouldRemoveItem() {
        // given
        List<CategorizedShoppingListItem> items = new ArrayList<>();
        items.add(CategorizedShoppingListItem.builder().name("Marchewka").build());
        sampleShoppingList.getItems().put(SAMPLE_CATEGORY, items);

        when(shoppingListRepository.findById(SAMPLE_ID))
                .thenReturn(Optional.of(sampleShoppingList));

        // when
        shoppingListService.removeItemFromCategory(SAMPLE_ID, SAMPLE_CATEGORY, 0);

        // then
        assertThat(sampleShoppingList.getItems()).isEmpty();
        verify(shoppingListRepository).save(sampleShoppingList);
    }

    @Test
    void removeItemFromCategory_WhenInvalidIndex_ShouldNotModifyList() {
        // given
        List<CategorizedShoppingListItem> items = new ArrayList<>();
        items.add(CategorizedShoppingListItem.builder().name("Marchewka").build());
        sampleShoppingList.getItems().put(SAMPLE_CATEGORY, items);

        when(shoppingListRepository.findById(SAMPLE_ID))
                .thenReturn(Optional.of(sampleShoppingList));

        // when
        shoppingListService.removeItemFromCategory(SAMPLE_ID, SAMPLE_CATEGORY, 1);

        // then
        assertThat(sampleShoppingList.getItems().get(SAMPLE_CATEGORY)).hasSize(1);
        verify(shoppingListRepository, never()).save(any());
    }

    @Test
    void addItemToCategory_WhenExists_ShouldAddItemAndReturn() {
        // given
        when(shoppingListRepository.findById(SAMPLE_ID))
                .thenReturn(Optional.of(sampleShoppingList));

        CategorizedShoppingListItem newItem = CategorizedShoppingListItem.builder()
                .name("Marchewka")
                .quantity(1.0)
                .unit("kg")
                .build();

        // when
        ShoppingList result = shoppingListService.addItemToCategory(SAMPLE_ID, SAMPLE_CATEGORY, newItem);

        // then
        assertThat(result.getItems().get(SAMPLE_CATEGORY))
                .hasSize(1)
                .contains(newItem);
        verify(shoppingListRepository).save(sampleShoppingList);
    }

    @Test
    void addItemToCategory_WhenNotExists_ShouldThrowNotFoundException() {
        // given
        when(shoppingListRepository.findById(SAMPLE_ID))
                .thenReturn(Optional.empty());

        CategorizedShoppingListItem newItem = CategorizedShoppingListItem.builder()
                .name("Marchewka")
                .build();

        // when/then
        assertThatThrownBy(() ->
                shoppingListService.addItemToCategory(SAMPLE_ID, SAMPLE_CATEGORY, newItem)
        )
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(SAMPLE_ID);
    }
}