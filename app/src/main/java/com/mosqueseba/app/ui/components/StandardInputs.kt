package com.mosqueseba.app.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosqueseba.app.ui.theme.*

/**
 * অ্যাপের সকল পেজ, স্ক্রিন ও মডিউলের জন্য স্ট্যান্ডার্ড ও অভিন্ন সার্চ বক্স।
 * স্ট্যান্ডার্ড সাইজ (min 50.dp), কর্নার রেডিয়াস (14.dp), থিম-অ্যাওয়ার কনটেইনার ও ক্লিয়ার বাটন সাপোর্ট করে।
 */
@Composable
fun MosqueSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "অনুসন্ধান করুন...",
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector = Icons.Default.Search,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val isDark = isSystemInDarkTheme()

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                fontSize = 13.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = "অনুসন্ধান",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(
                    onClick = { onValueChange("") },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "মুছুন",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else if (trailingContent != null) {
                trailingContent()
            }
        },
        singleLine = true,
        textStyle = TextStyle(
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = if (isDark) MosqueNightSurfaceVariant else Color.White,
            unfocusedContainerColor = if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft.copy(alpha = 0.35f),
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = if (isDark) MosqueNightBorder else MosqueCardBorder,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 50.dp)
    )
}

/**
 * অ্যাপের সকল ফর্ম এবং ডায়ালগের জন্য স্ট্যান্ডার্ড ইনপুট ফিল্ড কালার প্যালেট।
 */
@Composable
fun mosqueStandardFieldColors(isDark: Boolean = isSystemInDarkTheme()): TextFieldColors =
    OutlinedTextFieldDefaults.colors(
        focusedContainerColor = if (isDark) MosqueNightSurfaceVariant else Color.White,
        unfocusedContainerColor = if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft.copy(alpha = 0.35f),
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = if (isDark) MosqueNightBorder else MosqueCardBorder,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MosqueTextSecondary,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        cursorColor = MaterialTheme.colorScheme.primary
    )

/**
 * অ্যাপের সকল ফর্ম এবং স্ক্রিনের জন্য স্ট্যান্ডার্ড ও অভিন্ন টেক্সট ফিল্ড।
 * স্ট্যান্ডার্ড সাইজ (min 52.dp), প্রিমিয়াম কর্নার (14.dp) ও থিম-অ্যাওয়ার কনটেইনার প্রদান করে।
 */
@Composable
fun MosqueStandardTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String? = null,
    placeholder: String? = null,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    supportingText: (@Composable () -> Unit)? = null,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else 4,
    readOnly: Boolean = false,
    enabled: Boolean = true
) {
    val isDark = isSystemInDarkTheme()

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label?.let {
            {
                Text(
                    text = it,
                    fontSize = 13.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        placeholder = placeholder?.let {
            {
                Text(
                    text = it,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        isError = isError,
        supportingText = supportingText,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
        maxLines = maxLines,
        readOnly = readOnly,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        textStyle = TextStyle(
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        ),
        colors = mosqueStandardFieldColors(isDark),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
    )
}
