package com.lee.shoppe.ui.screens

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.lee.shoppe.R

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pages = listOf(
        OnboardingPage(
            R.drawable.img_1,
            R.string.onboarding_title_hello,
            R.string.onboarding_description_hello
        ),
        OnboardingPage(
            R.drawable.img_2,
            R.string.onboarding_title_enjoy,
            R.string.onboarding_description_enjoy
        )    )
    val pagerState = rememberPagerState()

    Box(modifier = Modifier.fillMaxSize()) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.ready_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Foreground content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 0.dp, top = 35.dp, end = 0.dp, bottom = 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Spacer(modifier = Modifier.height(35.dp))

            HorizontalPager(
                count = pages.size,
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPageContent(
                    page = pages[page],
                    isLastPage = page == pages.lastIndex,
                    onFinish = onFinish
                )
            }

            HorizontalPagerIndicator(
                pagerState = pagerState,
                activeColor = Color(0xFF004CFF),
                inactiveColor = Color(0xFFB3E5FC),
                indicatorWidth = 16.dp,
                indicatorHeight = 16.dp,
                spacing = 12.dp,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Spacer(modifier = Modifier.height(35.dp))


        }
    }
}

data class OnboardingPage(
    @DrawableRes val imageRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int
)

@Composable
fun OnboardingPageContent(
    page: OnboardingPage,
    isLastPage: Boolean,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .background(Color.White, shape = RoundedCornerShape(24.dp)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = page.imageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f) // You can tweak this value for preferred image ratio
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
        )

        Spacer(modifier = Modifier.height(45.dp))

        Text(
            text = stringResource(id = page.titleRes),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(id = page.descriptionRes),
            fontSize = 16.sp,
            color = Color.DarkGray,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            lineHeight = 22.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(30.dp))
        //Spacer(modifier = Modifier.weight(1f)) // Push button to bottom

        if (isLastPage) {
            Button(
                onClick = onFinish,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF004CFF),
                    contentColor = Color(0xFFF3F3F3)
                ),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(vertical = 24.dp)
                    .height(50.dp)
            ) {
                Text(
                    text = "Let's Start",
                    fontSize = 20.sp
                )
            }
        }
    }
}