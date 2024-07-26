
#include <unistd.h>

void
main (){

	unsigned char x[24] = {195, 161, 65, 65, 195, 161, 65, 65, 195, 178, 97, 108, 32, 42, 42, 10, 230, 232, 241, 123, 155, 254, 8, 64};

	write(1, &x, sizeof(x));
}
