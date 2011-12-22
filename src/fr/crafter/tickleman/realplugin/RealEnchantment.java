package fr.crafter.tickleman.realplugin;

public class RealEnchantment
{
	public static String enchantable(int stack) {
		switch(stack)
		{
     	case 256:
     	case 257:
     	case 258:
     	case 269:
     	case 270:
     	case 271:
     	case 273:
     	case 274:
     	case 275:
     	case 277:
     	case 278:
     	case 279:
     	case 284:
     	case 285:
     	case 286:
     		return "TOOL";
     	case 267:
     	case 268:
     	case 272:
     	case 276:
     	case 283:
     		return "WEAPON";
     	case 298:
     	case 302:
     	case 306:
     	case 310:
     	case 314:
     		return "ARMOR_HEAD";
     	case 299:
     	case 300:
     	case 303:
     	case 304:
     	case 307:
     	case 308:
     	case 311:
     	case 312:
     	case 315:
     	case 316:
     		return "ARMOR";
     	case 301:
     	case 305:
     	case 309:
     	case 313:
     	case 317:
     		return "ARMOR_FEET";
     	case 290:
     	case 291:
     	case 292:
     	case 293:
     	case 294:
     		return "HOE";
		}
		return "NADA";
	}
}
