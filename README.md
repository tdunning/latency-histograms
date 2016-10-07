# latency-histograms
An exploration of different kinds of variable scale histograms

The key idea being explored here is that the floating point representation of a number 
directly contains bits which can be used as an index into a histogram. When you do this, 
you get a very fast and simple data structure that allows nearly constant relative error 
in the original measurement space. 

The basic idea is similar to the idea of HdrHistograms, but the trick of (ab)using the floating
point hardware is, I think, novel.
