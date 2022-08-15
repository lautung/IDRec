//
// Created by 2630000966 on 2022/5/17.
//

#ifndef MYLIB_LOGGER_H
#define MYLIB_LOGGER_H


#include <unistd.h>
#include <sys/mman.h>
#include <string>

static int FILE_SIZE{getpagesize() * 256};

class Logger {

public:
    char *ptr{NULL};
     std::string fileDir{};
     std::string fileName{};
    int currentSize{};
public:
    Logger();

    int init();

    int write(const std::string &message);


    int closeMap();

    virtual ~Logger();

};

//Logger* logger = NULL;

//#ifdef __cplusplus
//extern "C" {
//#endif
//int Logger_init();
//#ifdef __cplusplus
//}
//#endif



//int Logger_init(){
//    logger = new Logger();
//    std::string fileDir = "/sdcard/Android/data/";
//    fileDir += "mp3cutter.ringtonemaker.audioeditor.mp3editor";
//    fileDir += "/files/mlog/";
//    logger->fileDir = fileDir;
//    logger->fileName = "log.txt";
//    logger->init();
//}




#endif //MYLIB_LOGGER_H
