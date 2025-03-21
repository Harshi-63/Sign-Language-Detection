package com.example.signlanguagedetection_app.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.signlanguagedetection_app.R
import com.example.signlanguagedetection_app.navigation.Router
import com.example.signlanguagedetection_app.navigation.Screen
import com.example.signlanguagedetection_app.ui.theme.BackgroundBlue

@Composable
fun HomeScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundBlue // Set background color for the screen
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Heading Section
            Text(
                text = "Welcome to Sign Language Translator",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(top = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description Section with Cards for Guidance
            Text(
                text = "Common Sign Language",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Cards for Instructions or Common Signs (Inspired by Example)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                InstructionCard(
                    imageResId = R.drawable.sign1, // Replace with your image resource ID
                    description = "Everything is fine"
                )
                InstructionCard(
                    imageResId = R.drawable.sign2, // Replace with your image resource ID
                    description = "I don't know"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                InstructionCard(
                    imageResId = R.drawable.sign3, // Replace with your image resource ID
                    description = "Point Of Objection"
                )
                InstructionCard(
                    imageResId = R.drawable.sign4, // Replace with your image resource ID
                    description = "Take Care"
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Translate Button Section (Commented Out)

            Button(
                onClick = { Router.navigateTo(Screen.CameraScreen) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Translate", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

        }
    }
}

@Composable
fun InstructionCard(imageResId: Int, description: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.size(150.dp) // Adjust size as needed for layout balance
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = null,
                modifier = Modifier.size(80.dp).padding(top = 8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewHomeScreen() {
    HomeScreen()
}
