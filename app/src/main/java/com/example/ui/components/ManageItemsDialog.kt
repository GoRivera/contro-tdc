package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Diálogo reutilizable para administrar listas personalizadas (Conceptos sugeridos, Personas/Beneficiarios).
 * Permite:
 * 1. Agregar nuevos elementos.
 * 2. Editar el texto de cualquier elemento existente.
 * 3. Eliminar elementos.
 * 4. Cambiar la posición/orden hacia arriba o hacia abajo.
 */
@Composable
fun ManageItemsDialog(
    title: String,
    subtitle: String,
    items: List<String>,
    icon: ImageVector = Icons.Default.Tune,
    itemLabel: String = "Concepto",
    minItems: Int = 1,
    onDismiss: () -> Unit,
    onSaveList: (List<String>) -> Unit
) {
    var currentList by remember { mutableStateOf(items.toList()) }
    var newItemText by remember { mutableStateOf("") }
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var editingText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                // Entrada para agregar nuevo elemento
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newItemText,
                        onValueChange = { newItemText = it },
                        label = { Text("Nuevo $itemLabel") },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("manage_items_new_input")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            val trimmed = newItemText.trim()
                            if (trimmed.isNotBlank() && !currentList.contains(trimmed)) {
                                currentList = currentList + trimmed
                                newItemText = ""
                            }
                        },
                        enabled = newItemText.isNotBlank(),
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("manage_items_add_btn")
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (newItemText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Agregar",
                                tint = if (newItemText.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Elementos actuales (${currentList.size}):",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Lista de elementos con reordenar, editar y eliminar
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(currentList) { index, item ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${index + 1}.",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.width(22.dp)
                                )

                                Text(
                                    text = item,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            editingIndex = index
                                            editingText = item
                                        }
                                )

                                // Subir posición
                                IconButton(
                                    onClick = {
                                        if (index > 0) {
                                            val mutable = currentList.toMutableList()
                                            val temp = mutable[index]
                                            mutable[index] = mutable[index - 1]
                                            mutable[index - 1] = temp
                                            currentList = mutable
                                        }
                                    },
                                    enabled = index > 0,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = "Mover arriba",
                                        modifier = Modifier.size(16.dp),
                                        tint = if (index > 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.outlineVariant
                                    )
                                }

                                // Bajar posición
                                IconButton(
                                    onClick = {
                                        if (index < currentList.size - 1) {
                                            val mutable = currentList.toMutableList()
                                            val temp = mutable[index]
                                            mutable[index] = mutable[index + 1]
                                            mutable[index + 1] = temp
                                            currentList = mutable
                                        }
                                    },
                                    enabled = index < currentList.size - 1,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = "Mover abajo",
                                        modifier = Modifier.size(16.dp),
                                        tint = if (index < currentList.size - 1) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.outlineVariant
                                    )
                                }

                                // Editar texto
                                IconButton(
                                    onClick = {
                                        editingIndex = index
                                        editingText = item
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Editar",
                                        modifier = Modifier.size(15.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                // Eliminar
                                IconButton(
                                    onClick = {
                                        if (currentList.size > minItems) {
                                            val mutable = currentList.toMutableList()
                                            mutable.removeAt(index)
                                            currentList = mutable
                                        }
                                    },
                                    enabled = currentList.size > minItems,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Eliminar",
                                        modifier = Modifier.size(16.dp),
                                        tint = if (currentList.size > minItems) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveList(currentList)
                    onDismiss()
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("manage_items_save_btn")
            ) {
                Text("Guardar Cambios", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )

    // Sub-diálogo para editar un elemento específico
    editingIndex?.let { idx ->
        AlertDialog(
            onDismissRequest = { editingIndex = null },
            title = { Text("Editar $itemLabel", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editingText,
                    onValueChange = { editingText = it },
                    label = { Text("Nombre del $itemLabel") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = editingText.trim()
                        if (trimmed.isNotBlank()) {
                            val mutable = currentList.toMutableList()
                            mutable[idx] = trimmed
                            currentList = mutable
                        }
                        editingIndex = null
                    },
                    enabled = editingText.isNotBlank()
                ) {
                    Text("Actualizar")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingIndex = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
