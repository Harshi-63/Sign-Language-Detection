package com.example.signlanguagedetection_app.components


import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.signlanguagedetection_app.R
import com.example.signlanguagedetection_app.ui.theme.DarkBtn
import com.example.signlanguagedetection_app.ui.theme.FocusedBorder
import com.example.signlanguagedetection_app.ui.theme.LightBtn
import com.example.signlanguagedetection_app.ui.theme.PrimaryTxt
import com.example.signlanguagedetection_app.ui.theme.PurpleGrey80
import com.example.signlanguagedetection_app.ui.theme.SecondaryTxt
import com.example.signlanguagedetection_app.ui.theme.focusedTextBox
import androidx.compose.material3.Text as Text1

@Composable
fun NormalTextComponent(value: String) {
    Text1(
        text = value,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp),
        style = TextStyle(
            fontSize = 20.sp,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.Normal
        ),
        color = SecondaryTxt,
        textAlign = TextAlign.Center
    )
}

@Composable
fun HeadingTextComponent(value: String) {
    Text1(
        text = value,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(),
        style = TextStyle(
            fontSize = 26.sp,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.Bold

        ),
        color = PrimaryTxt,
        textAlign = TextAlign.Center
    )
}

@Composable
fun MyTextfield(
    labelValue: String, painterResource: Painter, onTextSelected: (String) -> Unit,
    errorStatus: Boolean = false,
) {
    val textValue = remember {
        mutableStateOf("")
    }
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                shape = RoundedCornerShape(90.dp),
                color = Color.Transparent
            ),
        shape = RoundedCornerShape(34.dp),
        label = { Text(labelValue) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = FocusedBorder,
            focusedLabelColor = PrimaryTxt,
            focusedContainerColor = focusedTextBox,
            unfocusedContainerColor = Color.White
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        singleLine = true,
        maxLines = 1,
        value = textValue.value,
        //OutlinedTextField expects a String for value, not a MutableState<String>
        onValueChange = { newValue ->
            textValue.value = newValue
            onTextSelected(newValue)
        },
        leadingIcon = {
            Icon(painter = painterResource, contentDescription = "")
        },
        isError = !errorStatus
    )
}

@Composable
fun MyPwdField(
    labelValue: String,
    painterResource: Painter,
    onTextSelected: (String) -> Unit,
    errorStatus: Boolean = false,
) {
    val localFocusManager = LocalFocusManager.current
    val pwd = remember { mutableStateOf("") }
    val passwordVisible = remember { mutableStateOf(false) }

    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                shape = RoundedCornerShape(80.dp),
                color = Color.Transparent
            ),
        label = { Text(labelValue) },
        shape = RoundedCornerShape(34.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = FocusedBorder,
            focusedLabelColor = PrimaryTxt,
            focusedContainerColor = focusedTextBox,
            unfocusedContainerColor = Color.White
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        singleLine = true,
        keyboardActions = KeyboardActions {
            localFocusManager.clearFocus()
        },
        maxLines = 1,
        value = pwd.value,
        onValueChange = { newValue ->
            pwd.value = newValue
            onTextSelected(newValue)
        },
        leadingIcon = {
            Icon(painter = painterResource, contentDescription = null)
        },
        trailingIcon = {
            val iconImage = if (passwordVisible.value) {
                Icons.Filled.Visibility
            } else {
                Icons.Filled.VisibilityOff
            }
            val descriptor = if (passwordVisible.value) {
                stringResource(R.string.hide_pwd)
            } else {
                stringResource(R.string.show_pwd)
            }
            IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                Icon(imageVector = iconImage, contentDescription = descriptor)
            }
        },
        visualTransformation = if (passwordVisible.value) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        isError = !errorStatus
    )
}

@Composable
fun CheckboxComponent(
    value: String,
    onTextSelected: (String) -> Unit,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(56.dp)
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Keep track of the checked state
        val checkedState = remember { mutableStateOf(false) }

        // Custom Checkbox with Box and custom tick mark
        Box(
            modifier = Modifier
                .size(24.dp)
                .border(2.dp, color = Color.Gray, shape = RoundedCornerShape(4.dp))
                .background(
                    color = if (checkedState.value) Color(0xFF003B4A) else Color.Transparent, // Dark Blue when checked
                    shape = RoundedCornerShape(4.dp)
                )
                .clickable {
                    val newCheckedState = !checkedState.value
                    checkedState.value = newCheckedState
                    onCheckedChange(newCheckedState) // Invoke callback with the new state
                },
            contentAlignment = Alignment.Center
        ) {
            if (checkedState.value) {
                Icon(
                    imageVector = Icons.Default.Check, // White tick mark
                    contentDescription = "Checked",
                    tint = Color.White, // White color for tick mark
                    modifier = Modifier.size(16.dp) // Adjust tick mark size
                )
            }
        }

        // Text Component for the value
        ClickableTextComponent(value = value, onTextSelected)
    }
}



@Composable
fun ClickableTextComponent(value: String, onTextSelected: (String) -> Unit) {
    val initialText = " By continuing this you are agreeing our "
    val privPolicy = " Privacy Policy "
    val and = " and"
    val tAndCtext = " Terms and Conditions"
    val annotatedString = buildAnnotatedString {
        append(initialText)
        withStyle(style = SpanStyle(color = PrimaryTxt)) {
            pushStringAnnotation(tag = privPolicy, annotation = privPolicy)
            append(privPolicy)
        }
        append(and)
        withStyle(style = SpanStyle(color = PrimaryTxt)) {
            pushStringAnnotation(tag = tAndCtext, annotation = tAndCtext)
            append(tAndCtext)
        }
    }
    ClickableText(text = annotatedString, onClick = { offset ->
        annotatedString.getStringAnnotations(offset, offset)
            .firstOrNull()?.also { span ->
                Log.d("Clickable Text Component", "{$span}")

                if (span.item == tAndCtext || span.item == privPolicy) {
                    onTextSelected(span.item)
                }
            }
    })
}

@Composable
fun ButtonComponent(value: String, onButtonClicked: () -> Unit, isEnabled: Boolean = false) {
    Button(
        onClick = { onButtonClicked.invoke() },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(48.dp),
        contentPadding = PaddingValues(),
        colors = ButtonDefaults.buttonColors(Color.Transparent),
        enabled = isEnabled
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(48.dp)
                .background(
                    brush = Brush.horizontalGradient(listOf(DarkBtn, LightBtn)),
                    shape = RoundedCornerShape(50.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DividerTextComponent() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            color = PurpleGrey80,
            thickness = 1.dp
        )
        Text(
            modifier = Modifier.padding(8.dp),
            text = "or",
            fontSize = 18.sp,
            color = SecondaryTxt
        )
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            color = PurpleGrey80,
            thickness = 1.dp
        )
    }
}


@Composable
fun ClickableLoginTextComponent(tryingToLogin: Boolean = true, onTextSelected: (String) -> Unit) {
    val initialText =
        if (tryingToLogin) stringResource(R.string.already_have_an_account) else (stringResource(R.string.don_t_have_a_account_yet))
    val loginText = if (tryingToLogin) " Login" else " Register"
    val annotatedString = buildAnnotatedString {
        append(initialText)
        withStyle(style = SpanStyle(color = PrimaryTxt)) {
            pushStringAnnotation(tag = loginText, annotation = loginText)
            append(loginText)
        }

    }
    ClickableText(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp),
        style = TextStyle(
            fontSize = 15.sp,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center
        ),
        text = annotatedString, onClick = { offset ->
            annotatedString.getStringAnnotations(offset, offset)
                .firstOrNull()?.also { span ->
                    Log.d("Clickable Text Component", "{$span}")

                    if (span.item == loginText) {
                        onTextSelected(span.item)
                    }
                }
        })

}

@Composable
fun UnderlinedTextComponent(value: String) {
    Text1(
        text = value,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp),
        style = TextStyle(
            fontSize = 14.sp,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.Normal
        ),

        color = SecondaryTxt,
        textAlign = TextAlign.Center,
        textDecoration = TextDecoration.Underline
    )
}



