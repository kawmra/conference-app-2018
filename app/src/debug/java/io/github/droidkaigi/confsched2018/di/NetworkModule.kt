package io.github.droidkaigi.confsched2018.di

import com.facebook.stetho.okhttp3.StethoInterceptor
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import io.github.droidkaigi.confsched2018.data.api.DroidKaigiApi
import io.github.droidkaigi.confsched2018.data.api.FeedApi
import io.github.droidkaigi.confsched2018.data.api.FeedFirestoreApi
import io.github.droidkaigi.confsched2018.data.api.GithubApi
import io.github.droidkaigi.confsched2018.data.api.SessionFeedbackApi
import io.github.droidkaigi.confsched2018.data.api.response.mapper.ApplicationJsonAdapterFactory
import io.github.droidkaigi.confsched2018.data.api.response.mapper.LocalDateTimeAdapter
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.threeten.bp.LocalDateTime
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module internal object NetworkModule {

    @Singleton @Provides @JvmStatic
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .addNetworkInterceptor(HttpLoggingInterceptor()
                    .setLevel(HttpLoggingInterceptor.Level.BODY))
            .addNetworkInterceptor(StethoInterceptor())
            .build()

    @RetrofitDroidKaigi @Singleton @Provides @JvmStatic
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl("https://droidkaigi.jp/2018/sessionize/")
                .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder()
                        .add(ApplicationJsonAdapterFactory.INSTANCE)
                        .add(LocalDateTime::class.java, LocalDateTimeAdapter())
                        .build()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
                .build()
    }

    @RetrofitGoogleForm @Singleton @Provides @JvmStatic
    fun provideRetrofitForGoogleForm(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl("https://docs.google.com/forms/d/")
                .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder()
                        .add(ApplicationJsonAdapterFactory.INSTANCE)
                        .add(LocalDateTime::class.java, LocalDateTimeAdapter())
                        .build()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
                .build()
    }

    @RetrofitGithub @Singleton @Provides @JvmStatic
    fun provideRetrofitForGithub(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
                .baseUrl("https://api.github.com")
                .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder()
                        .add(ApplicationJsonAdapterFactory.INSTANCE)
                        .add(LocalDateTime::class.java, LocalDateTimeAdapter())
                        .build()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
                .client(okHttpClient)
                .build()
    }

    @Singleton @Provides @JvmStatic
    fun provideDroidKaigiApi(@RetrofitDroidKaigi retrofit: Retrofit): DroidKaigiApi {
        return retrofit.create(DroidKaigiApi::class.java)
    }

    @Singleton @Provides @JvmStatic
    fun provideFeedApi(): FeedApi = FeedFirestoreApi()

    @Singleton @Provides @JvmStatic
    fun provideSessionFeedbackApi(@RetrofitGoogleForm retrofit: Retrofit): SessionFeedbackApi {
        return retrofit.create(SessionFeedbackApi::class.java)
    }

    @Singleton @Provides @JvmStatic
    fun provideGithubApi(@RetrofitGithub retrofit: Retrofit): GithubApi {
        return retrofit.create(GithubApi::class.java)
    }
}
