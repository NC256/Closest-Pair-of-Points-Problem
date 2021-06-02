# What is this?

This is a solution to the [closest pair of points problem](https://en.wikipedia.org/wiki/Closest_pair_of_points_problem) in [O](https://en.wikipedia.org/wiki/Big_O_notation)(*n* log *n*) time complexity using a divide and conquer approach. The divide and conquer algorithm is based on the description contained in [Introduction to Algorithms, Third Edition](https://mitpress.mit.edu/books/introduction-algorithms-third-edition), section 33.4. The book credits [Computational Geometry: An Introduction](https://dl.acm.org/doi/10.5555/4333) as the source of the algorithm.

I wrote this code in late 2019 for my college algorithms class. It was one of my favorite assignments from that course because I was able to put in extra time to really understand the algorithm and implement it well. It was also really fun to see how much of an improvement the longer more complicated algorithm was over the extremely short brute force algorithm.

# What is the Closest Pair of Points Problem?

Imagine there are a bunch of dots on a piece of paper. How do you find the two dots that are closest together?

Admittedly not the most exciting problem, but it has some attributes that make it fun to work on:

* It's very simple to understand.
* Verifying you have the correct answer is easy to do.
* The brute force solution is very short.
* The divide-and-conquer solution is so much faster than the brute force solution.

# So how do you find the answer?


## Brute Force

It's worth looking at the brute force solution first. It works like this:

1. Compare all the points to each other.
2. As you go along, keep track of the closest points you're finding.
3. Once you've done every single comparison you should know which points are the closest.

A savvy reader might ask, "How do we 'compare' the points to each other?". And that's a good question. In real life we might pull out a ruler, but computers don't have that luxury. Fortunately, mathematicians figured it out a long time ago. The explanation of the equation is beyond this write-up, but you can find it on Wikipedia [here](https://en.wikipedia.org/wiki/Euclidean_distance#Two_dimensions).
### Code?

What's this look like in code?
Something like this (in Java):

```java
distance shortestSoFar = Double.POSITIVE_INFINITY;
for (Point p : thePoints){     // Loop over every point
  for (Point q : thePoints){   // For each point, loop through all the points again
    if (p == q){
      continue;  // Skip this loop since we don't want to compare a point to itself
    }
    double distance = distanceBetween(p,q); // Calculate the distance with some function (not shown here)
    if (distance < shortestSoFar){          // If it's shorter than the shortest we've found so far, save it
      shortestSoFar = distance;
    }
  }
}
```
This isn't meant to be exact code, but hopefully it gives you an example of what brute force looks like. You can write it slightly more efficiently, but no matter what you do with this approach the runtime complexity will always be O(n^2). That means that if you have 50 points to compare, this code will calculate the distance roughly 50\*50 times. That's not terrible, but if you were working with 100,000 points that would mean 100,000\*100,000 comparisons. 

Oh, and if you want to see the exact code, it's included in [the source code](src/Main.java). See the method called bruteForce, around line ~202.

How fast is Brute force then?
Number of points | Time Taken (On my computer)
------------ | -------------
50|0.0004 seconds
5,000|0.2 seconds
50,000|15 seconds
500,000|36 **minutes**

Uh oh, this is quickly becoming unusable. Let's see what Divide and Conquer has to offer:

## Divide and Conquer
 
This algorithm is, by comparison, much more complicated than the brute force algorithm. But it's worth it, trust me. It's *way* faster.

The main purpose of this algorithm is to find the same answer the brute force algorithm is finding, but with fewer comparisons.

Here are the steps with as little technical-mathy language as I could come up with:

1. Split your paper vertically so that half the points are on the left, and half the points are on the right. This might not be in the middle. It depends on where your points are. Just make sure that wherever you tear (in a straight vertical line), half are on the left piece and half are on the right piece.
2. Now split each of those pieces just like you did the first piece.
3. Keep splitting until your thin vertical strips of paper have exactly 2 or 3 points on them.
    1. If a piece has 2 points, then we know they are the closest on that individual piece.
    2. If a piece has 3 points, just compare all 3 to each other to find the closest two.
4. Once you've got a left and right piece of paper figured out like above, it's time to "combine" them. This is the step where we save on comparisons.
5. The only way there could be a closer pair of points on your combined piece of paper is if some of the points on the left or right pieces were close to the tear you made. So the only comparisons we need to make are with the points near the middle of the two pieces.
6. Keep recombining pieces of paper back together with their neighbors until you're back to the original. You should know the closest two points and you didn't have to check every single combination.

I glossed over a lot of details, but hopefully the way I worded things gave you an intuition for how the algorithm works. It's easier to understand with pictures, which I would add at some point if I move this explanation to a platform better suited for articles.

### Code?

The code for this algorithm is the entire purpose of this repository! I recommend you take a look at [the source code](src/Main.java), (the method is called divideAndConquer and starts around line 75 or so). It is very heavily commented because it was a requirement for the class, but hopefully that makes it easier to understand for people who haven't implemented this algorithm before.

So how much faster is it?

Here's the same table as before, but updated with the Divide and Conquer algorithm:

Number of points | Time Taken (Brute Force) | Time Taken (Divide And Conquer)
------------ | ------------- | -------------
50|0.0004 seconds|0.05 seconds
5,000|0.2 seconds|0.08 seconds
50,000|15 seconds|0.26 seconds
500,000|36 **minutes**|1.1 seconds
5,000,000| |13.2 seconds

I told you it was worth it!

Fun fact, brute force is actually faster when the number of points is small. The divide and conquer algorithm requires more setup and processing, which makes it a worse choice with a small number of points. When I first wrote the code I found through testing that anything below ~600 points was faster with brute force, but after that divide and conquer took the victory every single time.
