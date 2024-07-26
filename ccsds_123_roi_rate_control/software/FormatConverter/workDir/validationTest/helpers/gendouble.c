#include <unistd.h>

void
main () {

double x = 3.12432;

write(1, &x, sizeof(x));
}
