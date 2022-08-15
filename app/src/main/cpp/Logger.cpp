//
// Created by TONY on 2022/5/17.
//

#include <fcntl.h>
#include "Logger.h"
#include "android/log.h"

#define LOGE(TAG, FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR, TAG, FORMAT, ##__VA_ARGS__)

Logger::Logger() {
    LOGE("FFmpeg","Logger init");
}

Logger::~Logger() {
    if (ptr) {
        munmap(ptr, FILE_SIZE);
        delete ptr;
        ptr = nullptr;
    }
    LOGE("FFmpeg","Logger des");
}

int Logger::init() {
    int fd = open((fileDir + fileName).c_str(), O_RDWR | O_CREAT | O_TRUNC, 0644);
    if (-1 == fd) {
        perror("open file error");
        return -1;
    }
    ftruncate(fd, FILE_SIZE);
    ptr = (char *) mmap(nullptr, FILE_SIZE, PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0);
    if (ptr == MAP_FAILED) {
        perror("mmap error");
        ptr = nullptr;
        return -1;
    }
    close(fd);
    return 0;
}




int Logger::write(const std::string &message) {
    int result = 0;
//    if (FILE_SIZE - currentSize <= message.length()) {
//        //todo
//        currentSize = 0;
//        fileName = "log.txt.1";
//        result = init();
//        if (result == -1)return -1;
//    }
    LOGE("FFmpeg","%s",message.c_str());
    strcat(ptr, message.c_str());
    //currentSize += message.length();
    return result;
}

int Logger::closeMap() {
    int result = 0;
    if (ptr) {
        result = munmap(ptr, FILE_SIZE);
        if (result == -1) {
            perror("munmap error");
            return -1;
        }
    }
    return result;
}

