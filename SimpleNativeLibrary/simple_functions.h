//
// Created by benny on 2021/9/24.
//

#ifndef SIMPLENATIVELIBRARY__SIMPLE_FUNCTIONS_H_
#define SIMPLENATIVELIBRARY__SIMPLE_FUNCTIONS_H_

#include <stdlib.h>

typedef struct Person {
  long long id;
  char name[10];
  int age;
} Person;

typedef void (*OnEach)(int element);

void ForEach(int array[], int length, OnEach on_each);

void DumpPerson(Person *person);

int GetCLangVersion();



#endif //SIMPLENATIVELIBRARY__SIMPLE_FUNCTIONS_H_
