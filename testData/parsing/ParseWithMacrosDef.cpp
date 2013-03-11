void main () {

#define C() \
 do \
 { \
   int z = 0; \
   if (z) { \
   } \
   z = z * 2; \
 } \
 while (false)

 for(;;) {
 if (1) {
   C ();
 }
 }
 C ();
}