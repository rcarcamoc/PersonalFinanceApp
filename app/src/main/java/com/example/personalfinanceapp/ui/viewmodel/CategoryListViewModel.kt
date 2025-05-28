package com.example.personalfinanceapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personalfinanceapp.data.local.CategoryEntity
import com.example.personalfinanceapp.domain.usecase.category.GetCategoriesUseCase
import com.example.personalfinanceapp.domain.usecase.category.DeleteCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado para la pantalla de lista de categorías.
 */
data class CategoryListState(
    val isLoading: Boolean = false,
    val categories: List<CategoryEntity> = emptyList(),
    val error: String? = null
)

/**
 * ViewModel para la pantalla que muestra la lista de categorías.
 *
 * @property getCategoriesUseCase Caso de uso para obtener todas las categorías.
 * @property deleteCategoryUseCase Caso de uso para eliminar una categoría.
 */
@HiltViewModel
class CategoryListViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase // Para futuras funcionalidades de eliminar desde la lista
) : ViewModel() {

    private val _categoryListState = MutableStateFlow(CategoryListState())
    val categoryListState: StateFlow<CategoryListState> = _categoryListState.asStateFlow()

    init {
        loadCategories()
    }

    /**
     * Carga la lista de categorías desde el repositorio.
     */
    fun loadCategories() {
        viewModelScope.launch {
            getCategoriesUseCase()
                .onEach { categories ->
                    _categoryListState.value = CategoryListState(categories = categories)
                }
                .catch { e ->
                    _categoryListState.value = CategoryListState(error = e.message ?: "Error desconocido al cargar categorías")
                }
                .launchIn(viewModelScope)
        }
    }

    /**
     * Elimina una categoría.
     */
    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            try {
                deleteCategoryUseCase(category)
                // La lista se actualizará automáticamente si el Flow de getCategoriesUseCase está bien implementado
            } catch (e: Exception) {
                _categoryListState.value = _categoryListState.value.copy(error = e.message ?: "Error al eliminar la categoría")
            }
        }
    }
}

